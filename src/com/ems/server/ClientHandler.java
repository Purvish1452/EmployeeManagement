package com.ems.server;

import com.ems.service.EmployeeManager;
import com.ems.service.UserAuthenticationService;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Locale;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final UserAuthenticationService authenticationService;
    private UserAuthenticationService.UserSession session;

    public ClientHandler(Socket clientSocket,
                         UserAuthenticationService authenticationService) {
        this.clientSocket = clientSocket;
        this.authenticationService = authenticationService;
    }

    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));

             PrintWriter writer = new PrintWriter(
                     clientSocket.getOutputStream(), true)) {

            writer.println("Welcome. LOGIN|username|password or REGISTER|username|password");

            String command;

            while ((command = reader.readLine()) != null) {

                if ("EXIT".equalsIgnoreCase(command.trim())) {
                    writer.println("Goodbye!");
                    break;
                }

                writer.println(processCommand(command));
            }

        }catch (SocketTimeoutException e) {
            System.out.println("Client inactive for 60 seconds. Closing connection.");
        }
        catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String processCommand(String command) {

        try {

            String[] parts = command.split("\\|");
            String action = parts[0].toUpperCase(Locale.ROOT);

            switch (action) {

                case "LOGIN":
                case "REGISTER":
                    return authenticate(action, parts);

                case "ADD":
                case "SEARCH":
                case "UPDATE":
                case "DELETE":
                case "VIEW":
                case "PAYROLL":

                    if (session == null) {
                        throw new IllegalStateException("Please login first.");
                    }

                    return employeeCommand(action, parts);

                default:
                    return "Unknown command.";
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String authenticate(String action, String[] parts) throws IOException {

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid command.");
        }

        char[] password = parts[2].toCharArray();

        try {

            if ("LOGIN".equals(action)) {
                session = authenticationService.login(parts[1], password);
            } else {
                session = authenticationService.register(parts[1], password);
            }

            return "Authenticated as " + session.getUsername();

        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private String employeeCommand(String action, String[] parts)
            throws IOException {

        EmployeeManager manager = session.getEmployeeManager();

        switch (action) {

            case "ADD":

                requireLength(parts, 4);

                int id = manager.addPermanentEmployeeWithNextId(
                        parts[1],
                        parts[2],
                        Double.parseDouble(parts[3]));

                session.saveEmployees();

                return "Employee added. ID = " + id;

            case "SEARCH":

                requireLength(parts, 2);
                return manager.searchEmployee(
                        Integer.parseInt(parts[1])).toString();

            case "UPDATE":

                requireLength(parts, 3);

                manager.updateSalary(
                        Integer.parseInt(parts[1]),
                        Double.parseDouble(parts[2]));

                session.saveEmployees();

                return "Employee updated.";

            case "DELETE":

                requireLength(parts, 2);

                manager.removeEmployee(
                        Integer.parseInt(parts[1]));

                session.saveEmployees();

                return "Employee deleted.";

            case "VIEW":

                return manager.formatEmployees(
                        manager.getEmployees(),
                        "ALL EMPLOYEES");

            case "PAYROLL":

                EmployeeManager.PayrollSummary payroll =
                        manager.getPayrollSummary();

                return "Employees : " + payroll.getEmployeeCount()
                        + "\nSalary : " + payroll.getTotalSalary()
                        + "\nBonus : " + payroll.getTotalBonus()
                        + "\nPayroll : " + payroll.getTotalPayroll();

            default:
                return "Unknown command.";
        }
    }

    private void requireLength(String[] parts, int expected) {

        if (parts.length != expected) {
            throw new IllegalArgumentException("Invalid command format.");
        }
    }
}