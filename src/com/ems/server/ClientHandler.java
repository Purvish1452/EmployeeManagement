package com.ems.server;

import com.ems.service.EmployeeManager;
import com.ems.service.UserAuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

/** Processes one socket connection. Commands are rejected until LOGIN or REGISTER succeeds. */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UserAuthenticationService authenticationService;
    private UserAuthenticationService.UserSession session;

    public ClientHandler(Socket clientSocket, UserAuthenticationService authenticationService) {
        this.clientSocket = clientSocket;
        this.authenticationService = authenticationService;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            writer.println("Welcome. Authenticate with LOGIN|username|password or REGISTER|username|password.");
            String command;
            while ((command = reader.readLine()) != null) {
                if ("EXIT".equalsIgnoreCase(command.trim())) {
                    writer.println("Goodbye!");
                    return;
                }
                writer.println(processCommand(command));
            }
        } catch (IOException exception) {
            System.err.println("Client connection error: " + exception.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) { }
        }
    }

    private String processCommand(String command) {
        String[] parts = command.split("\\|", -1);
        String action = parts.length == 0 ? "" : parts[0].trim().toUpperCase(Locale.ROOT);
        try {
            if ("LOGIN".equals(action) || "REGISTER".equals(action)) {
                return authenticate(action, parts);
            }
            if (session == null) {
                throw new IllegalStateException("Please log in before using employee commands.");
            }
            return processEmployeeCommand(action, parts, session.getEmployeeManager());
        } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException exception) {
            return "Error: " + exception.getMessage();
        } catch (IOException exception) {
            return "Error: Could not save or load your employee data: " + exception.getMessage();
        }
    }

    private String authenticate(String action, String[] parts) throws IOException {
        if (parts.length != 3) {
            throw new IllegalArgumentException(action + " requires " + action + "|username|password.");
        }
        char[] password = parts[2].toCharArray();
        try {
            session = "LOGIN".equals(action) ? authenticationService.login(parts[1], password) : authenticationService.register(parts[1], password);
            return "Authenticated as " + session.getUsername() + ".";
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private String processEmployeeCommand(String action, String[] parts, EmployeeManager manager) throws IOException {
        if ("ADD".equals(action)) {
            requireLength(parts, 4, "ADD|Name|Department|Salary");
            int id = manager.addPermanentEmployeeWithNextId(parts[1], parts[2], Double.parseDouble(parts[3]));
            session.saveEmployees();
            return "Employee added successfully (ID: " + id + ").";
        }
        if ("SEARCH".equals(action)) {
            requireLength(parts, 2, "SEARCH|ID");
            return manager.searchEmployee(Integer.parseInt(parts[1])).toString();
        }
        if ("UPDATE".equals(action)) {
            requireLength(parts, 3, "UPDATE|ID|Salary");
            manager.updateSalary(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]));
            session.saveEmployees();
            return "Salary updated successfully.";
        }
        if ("DELETE".equals(action)) {
            requireLength(parts, 2, "DELETE|ID");
            manager.removeEmployee(Integer.parseInt(parts[1]));
            session.saveEmployees();
            return "Employee deleted successfully.";
        }
        if ("VIEW".equals(action)) return manager.formatEmployees(manager.getEmployees(), "ALL EMPLOYEES").replace(System.lineSeparator(), "[NEWLINE]");
        if ("PAYROLL".equals(action)) {
            EmployeeManager.PayrollSummary payroll = manager.getPayrollSummary();
            return "Total Employees: " + payroll.getEmployeeCount() + "[NEWLINE]Total Salary: " + payroll.getTotalSalary()
                    + "[NEWLINE]Total Bonus: " + payroll.getTotalBonus() + "[NEWLINE]Total Payroll: " + payroll.getTotalPayroll();
        }
        throw new IllegalArgumentException("Command not recognized.");
    }

    private void requireLength(String[] parts, int expected, String format) {
        if (parts.length != expected) throw new IllegalArgumentException("Invalid format. Expected " + format + ".");
    }
}
