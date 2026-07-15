package com.ems.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmployeeServer {
    private static final int PORT = 5000;
    private static final int THREAD_POOL_SIZE = 5;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        com.ems.service.EmployeeManager employeeManager = new com.ems.service.EmployeeManager();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server Started on port " + PORT + "...");

            // Add Shutdown Hook for Graceful Shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nInitiating graceful shutdown...");
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing ServerSocket: " + e.getMessage());
                }
                
                threadPool.shutdown();
                try {
                    if (!threadPool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                System.out.println("Server fully shut down.");
            }));

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket, employeeManager));
                } catch (java.net.SocketException e) {
                    // This exception is expected when serverSocket is closed via shutdown hook
                    if (serverSocket.isClosed()) {
                        System.out.println("ServerSocket closed, stopping accept loop.");
                    } else {
                        throw e;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            if (!threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        }
    }
}
