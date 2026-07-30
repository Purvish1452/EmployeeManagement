# Employee Management System

A thread-safe Java console and socket-based Employee Management System. It uses only standard Java exceptions and keeps each authenticated user's records isolated.

## Requirements

- Java 11 or newer

## Build and run

```bash
mkdir -p out
javac -d out $(find src -name "*.java")

# Local console application
java -cp out com.ems.EmployeeManagementSystem

# Or run the server, then connect one or more clients in other terminals
java -cp out com.ems.server.EmployeeServer
java -cp out com.ems.client.EmployeeClient
```

At startup, choose **register** to create a username/password, or **log in** to access an existing account. Usernames must be 3-32 letters, digits, `_`, or `-`; passwords must be at least eight characters.

## Authentication and storage

- A client must send `LOGIN|username|password` or `REGISTER|username|password` before it can use any employee command.
- Passwords are salted and PBKDF2-HMAC-SHA-256 hashed; they are not stored in plain text.
- User data is isolated at `data/users/<username>/employees.txt`. Credentials are stored alongside it in `credentials.txt`.
- Username validation and normalized paths prevent path traversal. The server never accepts a username as a file path.
- Each account has one shared, thread-safe `EmployeeManager`. It uses a single non-fair read/write lock to protect related standard collections and allows concurrent reads. A per-user lock only serializes account initialization and writes to that account's file.
- Employee records are immutable. Reads receive immutable snapshots, so data can safely be formatted, persisted, or iterated after the manager lock is released.

## Client commands

After authenticating, the socket client supports:

```text
ADD|Name|Department|Salary
SEARCH|EmployeeId
UPDATE|EmployeeId|Salary
DELETE|EmployeeId
VIEW
PAYROLL
EXIT
```

Server-created employee IDs are generated within the authenticated user's record set. No account can read or alter another account's manager or storage path.

## Error handling

There are no project-defined exception classes. Validation and service failures use built-in exceptions directly:

- `IllegalArgumentException` for invalid values and command formats
- `IllegalStateException` for duplicate IDs or use before login
- `NoSuchElementException` for missing employees/departments
- `NumberFormatException` for malformed numeric input
- `IOException` for persistence and network failures

The console client, standalone application, and server handler catch these exceptions at their interaction boundary and display their messages.

## Project layout

```text
src/com/ems/
├── EmployeeManagementSystem.java
├── client/EmployeeClient.java
├── enums/EmployeeType.java
├── model/
├── server/{ClientHandler,EmployeeServer}.java
├── service/{EmployeeManager,UserAuthenticationService}.java
└── util/
data/users/<username>/{credentials.txt,employees.txt}
```
