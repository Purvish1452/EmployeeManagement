package com.ems.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.*;

/**
 * Client that follows task-per-connection: opens a new socket for each protocol request.
 */
public class EmployeeClient {
    private static final Logger LOGGER = Logger.getLogger(EmployeeClient.class.getName());
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    private static String sessionToken = null;

    public static void main(String[] args) {
        // configure plain logging format for client
        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) {
            h.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    if (record.getLevel() == Level.SEVERE) return "[ERROR] " + record.getMessage() + System.lineSeparator();
                    if (record.getLevel() == Level.WARNING) return "[WARNING] " + record.getMessage() + System.lineSeparator();
                    return record.getMessage() + System.lineSeparator();
                }
            });
        }
        root.setLevel(Level.ALL);

        LOGGER.info("[CLIENT] Started");

        try (Scanner scanner = new Scanner(System.in)) {

            // Authenticate (open a connection only for login/register)
            if (!authenticate(scanner)) {
                LOGGER.info("[CLIENT] Exited");
                return;
            }

            while (true) {
                printMenu();
                String command = buildCommand(scanner);
                if (command == null) continue;

                if ("EXIT".equalsIgnoreCase(command)) {
                    // attempt logout if logged in
                    if (sessionToken != null) {
                    sendAndPrint("LOGOUT|" + sessionToken.trim());
                    }
                    LOGGER.info("[CLIENT] Exited");
                    return;
                }

                // All operations sent as REQUEST|sessionToken|... except LOGIN/REGISTER which were handled
                switch (command.split("\\|", 2)[0]) {
                    case "ADD":
                    case "SEARCH":
                    case "UPDATE":
                    case "DELETE":
                    case "VIEW":
                    case "PAYROLL":
                        if (sessionToken == null) {
                            System.out.println("Not authenticated. Please login or register first.");
                            break;
                        }
                        // Ensure token and command have no stray whitespace/newlines
                        String safeToken = sessionToken.trim();
                        String safeCommand = command.replaceAll("\r?\n", "").trim();
                        String request = "REQUEST|" + safeToken + "|" + safeCommand;
                        String response = sendAndReceive(request);
                        printResponse(response);
                        break;
                    default:
                        System.out.println("Unknown command.");
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected client error", e);
            System.err.println("Unexpected client error: " + e.getMessage());
        }
    }


    private static boolean authenticate(Scanner scanner) {
        // Loop until user authenticates successfully or chooses to exit.
        while (true) {
            System.out.print("Log in (L), register (R), or exit (E): ");
            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("E") || choice.equals("EXIT")) {
                return false; // user chose to exit
            }

            String action = choice.equals("R") ? "REGISTER" : "LOGIN";

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            String response = sendAndReceive(action + "|" + username + "|" + password);

            if (response == null) {
                System.out.println("No response from server. Try again or enter E to exit.");
                continue; // ask again
            }

            if (response.startsWith("SUCCESS|")) {
                // Trim whitespace/newlines from token to avoid protocol split across lines
                sessionToken = response.substring("SUCCESS|".length()).trim();
                System.out.println("Authenticated. Session token acquired.");
                return true;
            } else if (response.startsWith("ERROR|")) {
                // Show error but do not terminate; allow user to retry or exit
                System.out.println("Authentication failed: " + response.substring("ERROR|".length()));
                // loop again
                continue;
            } else {
                System.out.println("Unexpected response: " + response);
                continue;
            }
        }
    }

    private static String sendAndReceive(String message) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(message);

            // Read a single-line response from server. Server follows protocol and sends one line.
            String line = in.readLine();
            return line;

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Communication error: " + e.getMessage(), e);
            System.err.println("Communication error: " + e.getMessage());
            return null;
        }
    }

    private static void sendAndPrint(String message) {
        String response = sendAndReceive(message);
        printResponse(response);
    }

    private static void printResponse(String response) {
        if (response == null) return;
        if (response.startsWith("SUCCESS|")) {
            String payload = response.substring("SUCCESS|".length());
            // payload may use escaped newlines (\n) for multi-line content
            System.out.println(payload.replaceAll("\\\\n", "\n").replaceAll(";", "\n"));
        } else if (response.startsWith("ERROR|")) {
            System.out.println("Error: " + response.substring("ERROR|".length()));
        } else {
            System.out.println(response);
        }
    }

    private static void printMenu() {
        System.out.println("\n1 Add  2 Search  3 Update  4 Delete  5 View  6 Payroll  7 Exit");
    }

    private static String buildCommand(Scanner scanner) {
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("Name: ");
                String name = scanner.nextLine().trim();
                System.out.print("Department: ");
                String department = scanner.nextLine().trim();
                System.out.print("Salary: ");
                String salary = scanner.nextLine().trim();
                return "ADD|" + name + "|" + department + "|" + salary;
            case "2":
                System.out.print("Employee ID: ");
                return "SEARCH|" + scanner.nextLine().trim();
            case "3":
                System.out.print("Employee ID: ");
                String id = scanner.nextLine().trim();
                System.out.print("New salary: ");
                String newSalary = scanner.nextLine().trim();
                return "UPDATE|" + id + "|" + newSalary;
            case "4":
                System.out.print("Employee ID: ");
                return "DELETE|" + scanner.nextLine().trim();
            case "5":
                return "VIEW";
            case "6":
                return "PAYROLL";
            case "7":
                return "EXIT";
            default:
                System.out.println("Invalid choice.");
                return null;
        }
    }
}

