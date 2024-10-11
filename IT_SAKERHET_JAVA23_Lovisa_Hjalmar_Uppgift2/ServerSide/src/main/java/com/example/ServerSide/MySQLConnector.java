package com.example.ServerSide;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class MySQLConnector {
    public static void main(String[] args) {
        // Database credentials
        String url = "jdbc:mysql://localhost:8080/datasecurity";
        String user = "root";
        String password = "";

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection failed!");
        }
    }
}

