import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Scanner;
import java.io.*;

public class Main {
    private static final String SERVER_URL = "http://localhost:9090";

    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (choice != 3) {
            System.out.println("-------------");
            System.out.println("1. Create account.");
            System.out.println("2. Login.");
            System.out.println("3. Close program.");

            System.out.print("Choose a option: ");
            choice = Integer.parseInt(scanner.next());
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Email: ");
                    String email = scanner.nextLine();
                    System.out.println("Password: ");
                    String password = scanner.nextLine();
                    System.out.println("Message: ");
                    String message = scanner.nextLine();
                    try {
                        createUser(email, password, message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case 2:
                    System.out.println("Enter email: ");
                    String loginEmail = scanner.nextLine();
                    System.out.println("Enter password: ");
                    String loginPassword = scanner.nextLine();
                    try {
                        loginUser(loginEmail, loginPassword);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case 3:
                    scanner.close();
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Method to send POST request to create a new user
    private static void createUser(String email, String password, String message) throws IOException {
        String registerUrl = SERVER_URL + "/register";

        URL url = new URL(registerUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        String jsonInputString = String.format("{\"email\": \"%s\", \"password\": \"%s\", \"message\": \"%s\"}", email, password, message);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("User registration success.");
        } else {
            System.out.println("Registration failed. Response Code: " + responseCode);
        }
    }

    private static void loginUser(String email, String password) throws IOException {
        String loginUrl = SERVER_URL + "/login";
        URL url = new URL(loginUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        String jsonInputString = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scanner scanner = new Scanner(System.in);

        System.out.println("Login successful.");
        System.out.println("1. Read Message.");
        System.out.println("2. Delete User.");
        System.out.println("3. Close program.");
        System.out.print("Choose a option: ");
        int userChoice = Integer.parseInt(scanner.next());
        scanner.nextLine();

        switch (userChoice) {
            case 1:
                System.out.println("Enter email to show account: ");
                showUser();
                break;
            case 2:
                System.out.println("Enter email to delete account: ");
                String deleteEmail = scanner.nextLine();
                try {
                    deleteUser(deleteEmail);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 3:
                scanner.close();
                break;
        }
    }


    private static void showUser() throws IOException {
        String showUrl = SERVER_URL + "/user";
        URL url = new URL(showUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Show user info
            System.out.println("User information: " + content);
        } else {
            System.out.println("User not found. Response Code: " + responseCode);
        }
    }

    private static void deleteUser(String email) throws IOException {
        String deleteUrl = SERVER_URL + "/delete?email=" + email;
        URL url = new URL(deleteUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");


        int responseCode = conn.getResponseCode();
        if (responseCode == 204) {
            System.out.println("User deleted.");
        } else {
            System.out.println("Request failed. Response Code: " + responseCode);
        }
    }
}
