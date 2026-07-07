# Employee Management System

A simple console-based Java Employee Management System using OOP concepts and Java collections.

## Project Structure

```text
EmployeeManagementSystem/
|-- src/
|   `-- com/
|       `-- ems/
|           |-- model/
|           |   |-- Employee.java
|           |   |-- PermanentEmployee.java
|           |   |-- ContractEmployee.java
|           |   `-- Intern.java
|           |-- service/
|           |   `-- EmployeeManager.java
|           |-- exception/
|           |   |-- EmployeeNotFoundException.java
|           |   |-- DuplicateEmployeeException.java
|           |   |-- InvalidEmployeeTypeException.java
|           |   |-- InvalidEmployeeDataException.java
|           |   `-- DepartmentNotFoundException.java
|           |-- enums/
|           |   `-- EmployeeType.java
|           |-- util/
|           |   |-- InputUtil.java
|           |   |-- ValidationUtil.java
|           |   |-- FormatUtil.java
|           |   |-- MenuUtil.java
|           |   `-- PayrollUtil.java
|           `-- EmployeeManagementSystem.java
`-- README.md
```

## Compile and Run

From the project root:

```bash
javac -d out $(find src -name "*.java")
java -cp out com.ems.EmployeeManagementSystem
```

Employee records are saved automatically in `data/employees.txt` and loaded again when the program starts.
