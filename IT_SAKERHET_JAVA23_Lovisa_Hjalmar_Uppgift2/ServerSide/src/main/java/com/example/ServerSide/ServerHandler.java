package com.example.ServerSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerHandler {
    private static final int PORT = 8080;
    private static final HashMap<String, String> credentials = new HashMap<>();

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT);

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    handleClient(socket);

                } catch (IOException e) {
                    System.out.println(e);
                    socket.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            {
                serverSocket.close();
            }
        }

    }

    private static void handleClient(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String action = in.readLine(); // Read the action (login or register)
        if ("login".equals(action)) {
            // Handle login
            String email = in.readLine();
            String name = in.readLine();
            String password = in.readLine();

            if (credentials.containsKey(email) && credentials.get(email).equals(password)) {
                out.println("Success: Welcome " + name + "!");
            } else {
                out.println("Failure: Invalid credentials.");
            }
        }
    }

}

