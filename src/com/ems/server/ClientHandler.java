package com.ems.server;

import com.ems.service.EmployeeManager;
import com.ems.service.UserAuthenticationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientHandler for task-per-thread design: handles exactly one request per connection.
 */
public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final Socket clientSocket;
    private final UserAuthenticationService authenticationService;

    public ClientHandler(Socket clientSocket,
                         UserAuthenticationService authenticationService) {
        this.clientSocket = clientSocket;
        this.authenticationService = authenticationService;
    }

    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Read exactly one request from the client, respond, then terminate.
            String request = reader.readLine();
            System.out.println("SERVER RECEIVED: " + request);
            if (request == null) {
                // client closed without sending a request
                return; // client closed connection
            }

            // Read and process a single request; do not log every request to keep logs minimal.
            String response;
            try {
                response = processSingleRequest(request);
            } catch (Exception e) {
                // Unexpected exception during processing: log stack trace and send safe message
                LOGGER.log(Level.SEVERE, "Unexpected error processing request", e);
                response = "ERROR|Internal server error";
            }

            writer.println(response);

        } catch (IOException e) {
            // Connection-level IO issues are recoverable; log at WARNING
            LOGGER.log(Level.WARNING, "Connection error", e);
        } catch (Throwable t) {
            // Defensive: unexpected severe error
            LOGGER.log(Level.SEVERE, "Unexpected error handling client", t);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing client socket", e);
            }
            // Log disconnect once
            LOGGER.info("[CLIENT] Disconnected");
        }
    }

    private String processSingleRequest(String command) {
        try {
            String[] parts = command.split("\\|");
            String action = parts[0].toUpperCase(Locale.ROOT);

            switch (action) {
                case "LOGIN":
                    return handleLogin(parts);
                case "REGISTER":
                    return handleRegister(parts);
                case "REQUEST":
                    return handleRequest(parts);
                case "LOGOUT":
                    return handleLogout(parts);
                default:
                    LOGGER.warning("Invalid request");
                    return "ERROR|Unknown command.";
            }

        } catch (IllegalArgumentException e) {
            // Validation failure: warn and return client-friendly message
        LOGGER.warning("Invalid request");
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleLogin(String[] parts) {
        if (parts.length != 3) {
            return "ERROR|Invalid LOGIN format.";
        }

        char[] password = parts[2].toCharArray();
        try {
            String token = authenticationService.login(parts[1], password);
            return "SUCCESS|" + token;
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid login attempt");
            return "ERROR|" + e.getMessage();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during login", e);
            return "ERROR|Internal server error";
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private String handleRegister(String[] parts) {
        if (parts.length != 3) {
            return "ERROR|Invalid REGISTER format.";
        }

        char[] password = parts[2].toCharArray();
        try {
        String token = authenticationService.register(parts[1], password);
        return "SUCCESS|" + token;
        } catch (IllegalArgumentException e) {
        LOGGER.warning("Invalid registration attempt");
        return "ERROR|" + e.getMessage();
        } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Unexpected error during registration", e);
        return "ERROR|Internal server error";
        } finally {
        Arrays.fill(password, '\0');
        }
    }

    private String handleRequest(String[] parts) {
        System.out.println(Arrays.toString(parts));
        System.out.println("Length = " + parts.length);
        if (parts.length < 3) {
            return "ERROR|Invalid REQUEST format.";
        }

        String token = parts[1];
        UserAuthenticationService.UserSession session = authenticationService.getSession(token);
        if (session == null) {
            LOGGER.warning("Invalid session token");
            return "ERROR|Invalid or expired session token.";
        }

        EmployeeManager manager = session.getEmployeeManager();
        String op = parts[2].toUpperCase(Locale.ROOT);

        try {
            switch (op) {
                case "ADD":
                    if (parts.length != 6) { // REQUEST|token|ADD|name|dept|salary => 6 parts
                        return "ERROR|Invalid ADD format.";
                    }
                    String name = parts[3];
                    String dept = parts[4];
                    double salary = Double.parseDouble(parts[5]);
                    int id = manager.addPermanentEmployeeWithNextId(name, dept, salary);
                    try {
                        session.saveEmployees();
                    } catch (IOException e) {
                    // critical file operation failure
                    LOGGER.log(Level.SEVERE, "Unable to save employees: " + e.getMessage(), e);
                    return "ERROR|Unable to save employees: " + e.getMessage();
                    }
                    return "SUCCESS|Employee added. ID=" + id;

                case "SEARCH":
                    if (parts.length != 4) { // REQUEST|token|SEARCH|id
                        return "ERROR|Invalid SEARCH format.";
                    }
                    int searchId = Integer.parseInt(parts[3]);
                    return "SUCCESS|" + manager.searchEmployee(searchId).toString();

                case "UPDATE":
                    if (parts.length != 5) { // REQUEST|token|UPDATE|id|salary
                        return "ERROR|Invalid UPDATE format.";
                    }
                    int updateId = Integer.parseInt(parts[3]);
                    double newSalary = Double.parseDouble(parts[4]);
                    manager.updateSalary(updateId, newSalary);
                    try {
                        session.saveEmployees();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Unable to save employees: " + e.getMessage(), e);
                        return "ERROR|Unable to save employees: " + e.getMessage();
                    }
                    return "SUCCESS|Employee updated.";

                case "DELETE":
                    if (parts.length != 4) { // REQUEST|token|DELETE|id
                        return "ERROR|Invalid DELETE format.";
                    }
                    int deleteId = Integer.parseInt(parts[3]);
                    manager.removeEmployee(deleteId);
                    try {
                        session.saveEmployees();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Unable to save employees: " + e.getMessage(), e);
                        return "ERROR|Unable to save employees: " + e.getMessage();
                    }
                    return "SUCCESS|Employee deleted.";

                case "VIEW":
                    if (parts.length != 3) {
                        return "ERROR|Invalid VIEW format.";
                    }
                    String formatted = manager.formatEmployees(manager.getEmployees(), "ALL EMPLOYEES");
                    return "SUCCESS|" + formatted.replaceAll("\n", "\\n");

                case "PAYROLL":
                    if (parts.length != 3) {
                        return "ERROR|Invalid PAYROLL format.";
                    }
                    EmployeeManager.PayrollSummary payroll = manager.getPayrollSummary();
                    String payrollStr = "Employees:" + payroll.getEmployeeCount()
                            + ";Salary:" + payroll.getTotalSalary()
                            + ";Bonus:" + payroll.getTotalBonus()
                            + ";Payroll:" + payroll.getTotalPayroll();
                    return "SUCCESS|" + payrollStr;

                default:
                    LOGGER.warning("Invalid operation");
                    return "ERROR|Unknown operation.";
            }
        } catch (NumberFormatException e) {
        LOGGER.warning("Invalid numeric value in request");
            return "ERROR|Invalid numeric value in request.";
        } catch (Exception e) {
            // Unexpected error - will be handled at caller
            throw e;
        }
    }

    private String handleLogout(String[] parts) {
        if (parts.length != 2) {
            return "ERROR|Invalid LOGOUT format.";
        }

        boolean removed = authenticationService.logout(parts[1]);
        if (removed) {
            return "SUCCESS|Logged out.";
        } else {
            return "ERROR|Invalid or expired session token.";
        }
    }
}