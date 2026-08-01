package com.ems.server;

import com.ems.service.UserAuthenticationService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class EmployeeServer {
    private static final Logger LOGGER = Logger.getLogger(EmployeeServer.class.getName());
    private static final int PORT = 5000;
    private static final int THREAD_POOL_SIZE = 5;

    // Plain formatter that prints only the concise message lines required by the user
    private static class PlainFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String msg = record.getMessage();
            if (record.getLevel() == Level.SEVERE) {
                return "[ERROR] " + msg + System.lineSeparator();
            } else if (record.getLevel() == Level.WARNING) {
                return "[WARNING] " + msg + System.lineSeparator();
            }
            return msg + System.lineSeparator();
        }
    }

    @SuppressWarnings("try")
    public static void main(String[] args) {
        // configure global logging format once at startup
        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) {
            h.setFormatter(new PlainFormatter());
        }
        root.setLevel(Level.ALL);

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        UserAuthenticationService authenticationService = new UserAuthenticationService();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.info("[SERVER] Started on port " + PORT);

            // Shutdown Hook for Graceful Shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("[SERVER] Shutdown");
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error closing ServerSocket", e);
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
            }));

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(60000);
                    // single concise client connection log
                    LOGGER.info("[CLIENT] Connected");
                    threadPool.execute(new ClientHandler(clientSocket, authenticationService));
                } catch (java.net.SocketException e) {
                    if (serverSocket.isClosed()) {
                        break;
                    } else {
                        LOGGER.log(Level.WARNING, "Socket exception in accept loop", e);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "I/O exception in accept loop", e);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server failed to start", e);
        } finally {
            if (!threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        }
    }
}
