# Employee Management System

A simple, thread-safe Java Employee Management System with console and socket clients. Designed for clear separation of concerns, per-user isolation, and safe concurrent access.

## Project Description

Console and TCP socket-based system to manage employee records per authenticated user. Each user has isolated storage; server handles concurrent clients with per-user synchronization.

## Features

- Register / Login (PBKDF2 password hashing)
- Add, Update, Delete, Search, View employees
- Per-user immutable employee records and snapshot reads
- Thread-safe account isolation and file-based persistence
- Console and socket client modes

## Technologies Used

- Java 11+
- java.net (Sockets)
- java.util.concurrent (locks, executors)
- PBKDF2 (javax.crypto)
- Plain text file persistence (data/users/<username>/)

## Project Structure

src/com/ems/
- EmployeeManagementSystem.java (console entry)
- client/EmployeeClient.java
- server/EmployeeServer.java
- server/ClientHandler.java
- service/EmployeeManager.java
- service/UserAuthenticationService.java
- model/ (Employee, User, DTOs)
- util/ (crypto, I/O helpers)

data/users/<username>/
- credentials.txt
- employees.txt

## Architecture Overview

- Server accepts TCP connections; each connection → ClientHandler (thread from pool)
- Authentication service validates/creates users (salt + PBKDF2)
- Per-user EmployeeManager: single non-fair ReadWriteLock protecting in-memory collections and writes
- Immutable Employee objects; read operations return snapshots for safe concurrent use
- File persistence performed on write operations (atomic replace)

## Task-Per-Thread Workflow

- Server uses an ExecutorService (fixed/thread pool) to serve ClientHandlers.
- Each ClientHandler:
  1. Read socket input line
  2. Parse command
  3. If unauthenticated, require REGISTER/LOGIN
  4. Delegate business ops to per-user EmployeeManager
  5. Write responses back to client
- Per-user locks serialize writes; reads use shared read lock for concurrency.

## Authentication & Session Management

- Commands: REGISTER|username|password or LOGIN|username|password
- Passwords: salted PBKDF2-HMAC-SHA-256 stored in credentials.txt
- Successful LOGIN creates an in-memory session associated with the socket handler
- Session lasts until client disconnects or issues EXIT
- User data path validated and normalized to prevent traversal

## Communication Protocol (with request examples)

Plain text, pipe-separated commands over TCP (UTF-8), newline-terminated.

Authentication:
- REGISTER|alice|s3cr3t
- LOGIN|alice|s3cr3t

Employee operations (after login):
- ADD|John Doe|Engineering|75000
- SEARCH|emp-0001
- UPDATE|emp-0001|80000
- DELETE|emp-0001
- VIEW
- PAYROLL
- EXIT

Server responses are single-line status messages or multi-line payloads prefixed with OK / ERROR:
- OK|Employee added|emp-0001
- ERROR|Invalid command format

## How to Run the Server

Compile and run:
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.ems.server.EmployeeServer  # default port 9090
# or specify port:
java -cp out com.ems.server.EmployeeServer 9090
```

## How to Run the Client

Console client (local mode):
```bash
java -cp out com.ems.EmployeeManagementSystem
```
Socket client (connects to server):
```bash
java -cp out com.ems.client.EmployeeClient 127.0.0.1 9090
# then send protocol commands as specified
```

## Sample Console Output

Server:
```
[INFO] Server listening on port 9090
[INFO] Accepted connection from /127.0.0.1:52344
[DEBUG] ClientHandler-5: LOGIN request for 'alice' succeeded
```
Client (interactive):
```
> REGISTER|alice|s3cr3t
OK|Registered|alice
> LOGIN|alice|s3cr3t
OK|Authenticated
> ADD|Jane Doe|HR|65000
OK|Employee added|emp-0001
> VIEW
OK|1 records
emp-0001|Jane Doe|HR|65000
> EXIT
OK|Goodbye
```

## Key Java Concepts Used

- Concurrency: ExecutorService, ReadWriteLock, synchronized initialization
- Immutability: immutable Employee records and snapshot views
- Cryptography: PBKDF2 password hashing and secure random salts
- I/O: file-based persistence with atomic replacements, socket I/O
- Exception handling: input validation and boundary exception capture

## Future Improvements

- Replace file persistence with embedded DB (H2 or SQLite)
- Add TLS for socket communication
- REST API (HTTP) and browser client
- Role-based access control and audit logging
- Unit and integration tests with CI

## Author

Purvish (Purvish1452)
Repository: Purvish1452/EmployeeManagement

---

Updated on 2026-08-02
