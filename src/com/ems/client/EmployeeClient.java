package com.ems.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class EmployeeClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to Employee Server at " + SERVER_ADDRESS + ":" + SERVER_PORT);

            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine().trim();

                String command = buildCommand(choice, scanner);
                if (command == null) {
                    System.out.println("Invalid choice. Please try again.");
                    continue;
                }

                // Send String command
                serverOut.println(command);

                if ("EXIT".equalsIgnoreCase(command)) {
                    System.out.println("Disconnecting from server...");
                    break;
                }

                // Receive String response
                String response = serverIn.readLine();
                
                // Display response
                if (response != null) {
                    System.out.println("Server Response:\n" + response.replace("[NEWLINE]", "\n"));
                } else {
                    System.out.println("Server connection closed.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Employee Management Client Menu ---");
        System.out.println("1 Add");
        System.out.println("2 Search");
        System.out.println("3 Update");
        System.out.println("4 Delete");
        System.out.println("5 View");
        System.out.println("6 Payroll");
        System.out.println("7 Exit");
    }

    private static String buildCommand(String choice, Scanner scanner) {
        switch (choice) {
            case "1":
                System.out.print("Enter command (e.g., ADD|John|IT|50000): ");
                return scanner.nextLine().trim();
            case "2":
                System.out.print("Enter command (e.g., SEARCH|101): ");
                return scanner.nextLine().trim();
            case "3":
                System.out.print("Enter command (e.g., UPDATE|101|65000): ");
                return scanner.nextLine().trim();
            case "4":
                System.out.print("Enter command (e.g., DELETE|101): ");
                return scanner.nextLine().trim();
            case "5":
                return "VIEW";
            case "6":
                return "PAYROLL";
            case "7":
                return "EXIT";
            default:
                return null;
        }
    }
}
