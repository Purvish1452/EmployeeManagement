package com.ems.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/** Console client for the authenticated employee server protocol. */
public class EmployeeClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            System.out.println(serverIn.readLine());
            if (!authenticate(scanner, serverIn, serverOut)) return;
            while (true) {
                printMenu();
                String command = buildCommand(scanner);
                if (command == null) continue;
                serverOut.println(command);
                String response = serverIn.readLine();
                if (response == null) break;
                System.out.println(response.replace("[NEWLINE]", System.lineSeparator()));
                if ("EXIT".equalsIgnoreCase(command)) return;
            }
        } catch (IOException exception) {
            System.err.println("Client error: " + exception.getMessage());
        }
    }

    private static boolean authenticate(Scanner scanner, BufferedReader serverIn, PrintWriter serverOut) throws IOException {
        System.out.print("Log in (L) or register (R): ");
        String action = scanner.nextLine().trim().equalsIgnoreCase("R") ? "REGISTER" : "LOGIN";
        System.out.print("Username: "); String username = scanner.nextLine().trim();
        System.out.print("Password: "); String password = scanner.nextLine();
        serverOut.println(action + "|" + username + "|" + password);
        String response = serverIn.readLine();
        System.out.println(response);
        return response != null && response.startsWith("Authenticated as ");
    }

    private static void printMenu() {
        System.out.println("\n1 Add  2 Search  3 Update  4 Delete  5 View  6 Payroll  7 Exit");
    }

    private static String buildCommand(Scanner scanner) {
        System.out.print("Enter choice: ");
        switch (scanner.nextLine().trim()) {
            case "1": System.out.print("Name: "); String name = scanner.nextLine().trim(); System.out.print("Department: "); String department = scanner.nextLine().trim(); System.out.print("Salary: "); return "ADD|" + name + "|" + department + "|" + scanner.nextLine().trim();
            case "2": System.out.print("Employee ID: "); return "SEARCH|" + scanner.nextLine().trim();
            case "3": System.out.print("Employee ID: "); String id = scanner.nextLine().trim(); System.out.print("New salary: "); return "UPDATE|" + id + "|" + scanner.nextLine().trim();
            case "4": System.out.print("Employee ID: "); return "DELETE|" + scanner.nextLine().trim();
            case "5": return "VIEW";
            case "6": return "PAYROLL";
            case "7": return "EXIT";
            default: System.out.println("Invalid choice."); return null;
        }
    }
}
