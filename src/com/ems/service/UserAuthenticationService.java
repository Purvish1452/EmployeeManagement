package com.ems.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/** Handles registration, login, and the employee data belonging to each user.
 *  Now also manages active sessions using session tokens (UUIDs).
 */
public class UserAuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(UserAuthenticationService.class.getName());
    private static final Path USERS_DIRECTORY = Path.of("data", "users").toAbsolutePath().normalize();

    private final Map<String, UserData> users = new HashMap<>();

    // Active sessions: token -> UserSession
    private final ConcurrentHashMap<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Register user and create a new session token.
     * Returns session token on success.
     */
    public String register(String username, char[] password) throws IOException {
        validateCredentials(username, password);

        UserData userData = getUserData(username);

        synchronized (userData) {
            Path credentialsFile = userData.directory.resolve("credentials.txt");

            if (Files.exists(credentialsFile)) {
                throw new IllegalStateException("That username is already registered.");
            }

            Files.createDirectories(userData.directory);

            String credentials = username + ":" + hashPassword(password);

            Files.writeString(
                    credentialsFile,
                    credentials,
                    StandardCharsets.UTF_8);

            userData.employeesLoaded = true; // new user, nothing to load

            UserSession session = new UserSession(username, userData);
            return createSession(session);
        }
    }

    /**
     * Login user and create a new session token.
     * Returns session token on success.
     */
    public String login(String username, char[] password) throws IOException {
        validateCredentials(username, password);

        UserData userData = getUserData(username);

        synchronized (userData) {

            Path credentialsFile = userData.directory.resolve("credentials.txt");

            if (!Files.exists(credentialsFile)) {
                throw new IllegalArgumentException(
                        "Invalid username or password.");
            }

            String line =
                    Files.readString(credentialsFile,
                            StandardCharsets.UTF_8);

            String[] parts = line.split(":", 2);

            if (parts.length != 2) {
                LOGGER.severe("Malformed credentials file");
                throw new IOException("Invalid credentials file.");
            }

            String storedUsername = parts[0];
            String storedPasswordHash = parts[1];

            if (!storedUsername.equals(username)
                    || !storedPasswordHash.equals(hashPassword(password))) {
                throw new IllegalArgumentException(
                        "Invalid username or password.");
            }

            if (!userData.employeesLoaded) {
                userData.employeeManager.loadEmployeesFromFile(
                        userData.employeeFile);

                userData.employeesLoaded = true;
            }

            UserSession session = new UserSession(username, userData);
            return createSession(session);
        }
    }

    private String createSession(UserSession session) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, session);
        return token;
    }

    /**
     * Returns the UserSession for a token or null if not present.
     */
    public UserSession getSession(String token) {
        if (token == null) return null;
        return activeSessions.get(token);
    }

    /**
     * Invalidate a session token. Returns true if removed.
     */
    public boolean logout(String token) {
        if (token == null) return false;
        boolean removed = activeSessions.remove(token) != null;
        return removed;
    }

    private UserData getUserData(String username) {
        synchronized (users) {
            UserData userData = users.get(username);

            if (userData == null) {
                userData = new UserData(getUserDirectory(username));
                users.put(username, userData);
            }

            return userData;
        }
    }

    private void validateCredentials(String username, char[] password) {
        if (username == null || !username.matches("[A-Za-z0-9_-]{3,32}")) {
            throw new IllegalArgumentException("Username must contain 3-32 letters, digits, underscores, or hyphens.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
    }

    private Path getUserDirectory(String username) {
        Path directory = USERS_DIRECTORY.resolve(username).normalize();
        if (!directory.startsWith(USERS_DIRECTORY)) {
            throw new IllegalArgumentException("Invalid username.");
        }
        return directory;
    }
    private String hashPassword(char[] password) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    new String(password)
                            .getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();

            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 not available.",
                    exception);
        }
    }

    public static final class UserSession {
        private final String username;
        private final UserData userData;

        private UserSession(String username, UserData userData) {
            this.username = username;
            this.userData = userData;
        }

        public String getUsername() {
            return username;
        }

        public EmployeeManager getEmployeeManager() {
            return userData.employeeManager;
        }

        public void saveEmployees() throws IOException {
            synchronized (userData) {
                userData.employeeManager.saveEmployeesToFile(userData.employeeFile);
            }
        }
    }

    /** Holds the data shared by all sessions for one username. */
    private static final class UserData {
        private final Path directory;
        private final Path employeeFile;
        private final EmployeeManager employeeManager;
        private boolean employeesLoaded;

        private UserData(Path directory) {
            this.directory = directory;
            this.employeeFile = directory.resolve("employees.txt");
            this.employeeManager = new EmployeeManager();
        }
    }
}
