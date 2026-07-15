# Employee Management System

A console-based Java Employee Management System built using object-oriented design, custom exceptions, and Java collections.

## Overview

This application allows users to manage employee records from the command line. It supports different employee types, persistent storage, validation, and payroll-related information.

## Features

- Add new employees as:
  - Permanent Employee
  - Contract Employee
  - Intern
- View all employee records
- Search employees by ID or department
- Update and delete employee details
- Validate input data and handle invalid entries
- Save employee records to `data/employees.txt`
- Load employee records automatically at startup

## Concepts Used

- Object-Oriented Programming:
  - Encapsulation with private fields and public getters/setters
  - Inheritance via `Employee` base class and `PermanentEmployee`, `ContractEmployee`, `Intern`
  - Abstraction through the abstract `Employee` model and service interfaces
  - Polymorphism by handling different employee subclasses through the `Employee` type
- Exception Handling:
  - Custom checked exceptions such as `DuplicateEmployeeException`, `EmployeeNotFoundException`, `InvalidEmployeeDataException`, `DepartmentNotFoundException`, and `InvalidEmployeeTypeException`
  - Validation and controlled error handling during input, search, update, and file operations
- Collections:
  - `List`, `Map`, `Set`, `TreeSet`, and concurrent collections such as `ConcurrentHashMap`
  - Collection-based search, sorting, and department grouping
- File Handling:
  - Persistent storage using `java.nio.file.Files`
  - Reading and writing employee records from `data/employees.txt`
  - Base64 encoding/decoding for safe record serialization
- Enums:
  - `EmployeeType` enum for employee category selection and validation
- Multithreading / Concurrency:
  - Server-side thread pool with `ExecutorService` and `Executors.newFixedThreadPool`
  - Client request handling using `Runnable` in `ClientHandler`
  - Concurrent data structures and thread-safe employee management for multiple clients

## Prerequisites

- Java JDK 8 or newer
- Unix-like shell (Linux, macOS) or Windows command prompt / PowerShell

## Build and Run

From the project root directory:

```bash
javac -d out $(find src -name "*.java")
java -cp out com.ems.EmployeeManagementSystem
```

Alternatively, compile and run in one command:

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.ems.EmployeeManagementSystem
```

## Usage

When the application starts, a menu is displayed. Use the menu to perform actions such as:

1. Add an employee
2. View all employees
3. Search by employee ID
4. Search by department
5. Update employee details
6. Delete an employee
7. Exit

Employee data is persisted to `data/employees.txt`, so records remain available between runs.

## Project Structure

```text
EmployeeManagementSystem/
├── data/
│   └── employees.txt
├── src/
│   └── com/
│       └── ems/
│           ├── client/
│           │   └── EmployeeClient.java
│           ├── enums/
│           │   └── EmployeeType.java
│           ├── exception/
│           │   ├── DepartmentNotFoundException.java
│           │   ├── DuplicateEmployeeException.java
│           │   ├── EmployeeNotFoundException.java
│           │   ├── InvalidEmployeeDataException.java
│           │   └── InvalidEmployeeTypeException.java
│           ├── model/
│           │   ├── ContractEmployee.java
│           │   ├── Employee.java
│           │   ├── Intern.java
│           │   └── PermanentEmployee.java
│           ├── server/
│           │   ├── ClientHandler.java
│           │   └── EmployeeServer.java
│           ├── service/
│           │   └── EmployeeManager.java
│           ├── util/
│           │   ├── FormatUtil.java
│           │   ├── InputUtil.java
│           │   ├── MenuUtil.java
│           │   ├── PayrollUtil.java
│           │   └── ValidationUtil.java
│           ├── EmployeeManagementSystem.java
│           └── client/
│               └── EmployeeClient.java
└── README.md
```

## Notes

- If `data/employees.txt` does not exist, the system creates it automatically when saving records.
- Keep the `out` directory separate from source files for compiled classes.

## License

This project is provided for educational use and demonstration purposes.
