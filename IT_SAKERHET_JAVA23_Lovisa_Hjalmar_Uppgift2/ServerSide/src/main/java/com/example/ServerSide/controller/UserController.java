package com.example.ServerSide.controller;

import com.example.ServerSide.util.EncryptionUtil;
import com.example.ServerSide.util.JwtUtil;
import com.example.ServerSide.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.sql.PreparedStatement;

@RestController
public class UserController {


    @RequestMapping("/api/auth")
    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:4306/datasecurity", "root", "");
        } catch (Exception e) {
            System.out.println("FEL: " + e);
            return null;
        }
    }

    //Method for hashing password using SHA-256
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam String email) throws SQLException {
        String query = "DELETE FROM users WHERE email = ?";
        PreparedStatement ps = Objects.requireNonNull(connect()).prepareStatement(query);
        ps.setString(1, email);
        int userDeleted = ps.executeUpdate();
        if (userDeleted > 0) {
            System.out.println("Account deleted");
            return ResponseEntity.noContent().build();
        } else {
            System.out.println("Error.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) throws SQLException {
        // finds user by email and hashed password
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        PreparedStatement ps = Objects.requireNonNull(connect()).prepareStatement(query);
        String hashedPassword = hashPassword(user.getPassword());

        ps.setString(1, user.getEmail());
        ps.setString(2, hashedPassword);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            JwtUtil jwtUtil = new JwtUtil();
            String token = jwtUtil.generateToken(user.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response.toString());
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Login failed! Check your email and password and try agin!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse.toString());
        }
    }


    @GetMapping("/user")
    public ResponseEntity<User> showUser(@RequestHeader("Authorization") String authHeader, @RequestParam String email) throws SQLException {
        String token = authHeader.substring(7);
        System.out.println("Token recieved" + token);
        JwtUtil jwtUtil = new JwtUtil();

        if (jwtUtil.validateToken(token, email)) {
            System.out.println("token validated");
            String query = "SELECT email, message FROM users WHERE email = ?";
            PreparedStatement ps = Objects.requireNonNull(connect()).prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                String encryptedMessage = rs.getString("message");
                String decryptMessage = null;
                try {
                    decryptMessage = EncryptionUtil.decrypt(encryptedMessage);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                user.setMessage(decryptMessage);
                return ResponseEntity.ok().body(user);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @RequestMapping("/register")
    public void addUser(@RequestBody User user) throws SQLException {
        try {
            String encryptedMessage = EncryptionUtil.encrypt(user.getMessage());
            user.setMessage(encryptedMessage);

            String hashedPassword = hashPassword(user.getPassword());

            String query = "INSERT INTO users(email, password, message) VALUES (?, ?, ?)";
            PreparedStatement ps = Objects.requireNonNull(connect()).prepareStatement(query);
            ps.setString(1, user.getEmail());
            ps.setString(2, hashedPassword);
            ps.setString(3, encryptedMessage);
            int addUser = ps.executeUpdate();

            if (addUser > 0) {
                System.out.println("User added.");
            } else {
                System.out.println("Error.");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

}
