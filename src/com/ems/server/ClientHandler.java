package com.ems.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Object OUTPUT_LOCK = new Object();
    private static final java.util.concurrent.atomic.AtomicInteger idGenerator = new java.util.concurrent.atomic.AtomicInteger(100);

    private final Socket clientSocket;
    private final com.ems.service.EmployeeManager employeeManager;

    public ClientHandler(Socket clientSocket, com.ems.service.EmployeeManager employeeManager) {
        this.clientSocket = clientSocket;
        this.employeeManager = employeeManager;
    }

    @Override
    public void run() {
        String clientIP = clientSocket.getInetAddress().getHostAddress();
        int clientPort = clientSocket.getPort();
        String threadName = Thread.currentThread().getName();

        System.out.printf("[%s] %s:%d Connected%n", threadName, clientIP, clientPort);

        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String command;
            while ((command = reader.readLine()) != null) {
                System.out.printf("[%s] Request from %s:%d - %s%n", threadName, clientIP, clientPort, command);

                if ("EXIT".equalsIgnoreCase(command)) {
                    writer.println("Goodbye!");
                    break;
                }
                
                String response = processCommand(command);
                writer.println(response);
            }
        } catch (IOException e) {
            System.err.println(String.format("[%s] Error with %s:%d - %s", threadName, clientIP, clientPort, e.getMessage()));
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                System.out.printf("[%s] %s:%d Disconnected%n", threadName, clientIP, clientPort);
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private String processCommand(String command) {
        String[] parts = command.split("\\|");
        String action = parts[0].toUpperCase();

        try {
            switch (action) {
                case "ADD":
                    if (parts.length >= 4) {
                        String name = parts[1];
                        String dept = parts[2];
                        double salary = Double.parseDouble(parts[3]);
                        int id = idGenerator.incrementAndGet();
                        com.ems.model.Employee emp = new com.ems.model.PermanentEmployee(id, name, dept, salary, 1);
                        employeeManager.addEmployee(emp);
                        return "Employee Added Successfully (ID: " + id + ")";
                    }
                    return "Error: Invalid ADD format. Expected ADD|Name|Department|Salary";
                
                case "SEARCH":
                    if (parts.length >= 2) {
                        int id = Integer.parseInt(parts[1]);
                        com.ems.model.Employee emp = employeeManager.searchEmployee(id);
                        return emp.toString();
                    }
                    return "Error: Invalid SEARCH format. Expected SEARCH|ID";

                case "UPDATE":
                    if (parts.length >= 3) {
                        int id = Integer.parseInt(parts[1]);
                        double newSalary = Double.parseDouble(parts[2]);
                        employeeManager.updateSalary(id, newSalary);
                        return "Salary Updated Successfully";
                    }
                    return "Error: Invalid UPDATE format. Expected UPDATE|ID|Salary";

                case "DELETE":
                    if (parts.length >= 2) {
                        int id = Integer.parseInt(parts[1]);
                        employeeManager.removeEmployee(id);
                        return "Employee Deleted Successfully";
                    }
                    return "Error: Invalid DELETE format. Expected DELETE|ID";

                case "VIEW":
                case "PAYROLL":
                    return captureSystemOut(() -> {
                        if (action.equals("VIEW")) {
                            employeeManager.displayAllEmployees();
                        } else {
                            employeeManager.calculateTotalPayroll();
                        }
                    });

                default:
                    return "Error: Command not recognized";
            }
        } catch (com.ems.exception.EmployeeNotFoundException | 
                 com.ems.exception.DuplicateEmployeeException | 
                 com.ems.exception.InvalidEmployeeDataException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "System Error: " + e.getMessage();
        }
    }

    private String captureSystemOut(Runnable task) {
        synchronized (OUTPUT_LOCK) {
            java.io.PrintStream originalOut = System.out;
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try {
                System.setOut(new java.io.PrintStream(baos));
                task.run();
                return baos.toString().replace("\n", "[NEWLINE]").replace("\r", "");
            } finally {
                System.setOut(originalOut);
            }
        }
    }
}
