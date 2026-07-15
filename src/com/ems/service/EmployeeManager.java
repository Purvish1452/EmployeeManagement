package com.ems.service;

import com.ems.exception.DepartmentNotFoundException;
import com.ems.exception.DuplicateEmployeeException;
import com.ems.exception.EmployeeNotFoundException;
import com.ems.exception.InvalidEmployeeDataException;
import com.ems.model.ContractEmployee;
import com.ems.model.Employee;
import com.ems.model.Intern;
import com.ems.model.PermanentEmployee;
import com.ems.util.FormatUtil;
import com.ems.util.PayrollUtil;
import com.ems.util.ValidationUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EmployeeManager {
    // Use the ReadWriteLock interface while keeping the concrete ReentrantReadWriteLock implementation.
    // This preserves reentrancy, so a thread that already holds the write lock can safely call addEmployee()
    // during a bulk load without deadlocking.
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private final List<Employee> employees;
    private final Map<Integer, Employee> employeeMap;
    private final Set<String> departments;

    public EmployeeManager() {
        employees = new ArrayList<>();
        employeeMap = new ConcurrentHashMap<>();
        departments = ConcurrentHashMap.newKeySet();
    }

    public void addEmployee(Employee employee) throws DuplicateEmployeeException, InvalidEmployeeDataException {
        writeLock.lock();
        try {
            ValidationUtil.validateEmployee(employee);

            if (employeeMap.containsKey(employee.getEmpId())) {
                throw new DuplicateEmployeeException(employee.getEmpId());
            }

            employees.add(employee);
            employeeMap.put(employee.getEmpId(), employee);
            departments.add(employee.getDepartment());
        } finally {
            writeLock.unlock();
        }
    }

    public void removeEmployee(int empId) throws EmployeeNotFoundException {
        writeLock.lock();
        try {
            Employee employee = employeeMap.get(empId);

            if (employee == null) {
                throw new EmployeeNotFoundException(empId);
            }

            employees.remove(employee);
            employeeMap.remove(empId);
            rebuildDepartments();
        } finally {
            writeLock.unlock();
        }
    }

    public Employee searchEmployee(int empId) throws EmployeeNotFoundException {
        readLock.lock();
        try {
            Employee employee = employeeMap.get(empId);

            if (employee == null) {
                throw new EmployeeNotFoundException(empId);
            }

            return employee;
        } finally {
            readLock.unlock();
        }
    }

    public void displayAllEmployees() {
        readLock.lock();
        try {
            if (employees.isEmpty()) {
                System.out.println("No employees in the system!");
                return;
            }

            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println("ALL EMPLOYEES");
            System.out.println("=".repeat(80));

            for (Employee employee : employees) {
                System.out.println(employee);
            }

            System.out.println("=".repeat(80));
        } finally {
            readLock.unlock();
        }
    }

    public void displayByDepartment(String department) throws DepartmentNotFoundException {
        readLock.lock();
        try {
            var departmentEmployees = new ArrayList<Employee>(); // var is clear because the ArrayList constructor shows the element type.

            for (Employee employee : employees) {
                if (employee.getDepartment().equalsIgnoreCase(department)) {
                    departmentEmployees.add(employee);
                }
            }

            if (departmentEmployees.isEmpty()) {
                throw new DepartmentNotFoundException(department);
            }

            System.out.println();
            System.out.println(FormatUtil.line(80, "="));
            System.out.println("EMPLOYEES IN " + department.toUpperCase() + " DEPARTMENT");
            System.out.println(FormatUtil.line(80, "="));

            for (Employee employee : departmentEmployees) {
                System.out.println(employee);
            }

            System.out.println(FormatUtil.line(80, "="));
        } finally {
            readLock.unlock();
        }
    }

    public void displayAllDepartments() {
        readLock.lock();
        try {
            if (departments.isEmpty()) {
                System.out.println("No departments found!");
                return;
            }

            System.out.println("All Departments: " + departments);
        } finally {
            readLock.unlock();
        }
    }

    public void calculateTotalPayroll() {
        readLock.lock();
        try {
            var totalSalary = PayrollUtil.calculateTotalSalary(employees); // var is clear because PayrollUtil returns a double total.
            var totalBonus = PayrollUtil.calculateTotalBonus(employees); // var is clear because PayrollUtil returns a double total.

            System.out.println();
            System.out.println(FormatUtil.line(50, "="));
            System.out.println("PAYROLL SUMMARY");
            System.out.println(FormatUtil.line(50, "="));
            System.out.println("Total Employees: " + employees.size());
            System.out.println("Total Salary: " + FormatUtil.currency(totalSalary));
            System.out.println("Total Bonus: " + FormatUtil.currency(totalBonus));
            System.out.println("Total Payroll: " + FormatUtil.currency(totalSalary + totalBonus));
            System.out.println(FormatUtil.line(50, "="));
        } finally {
            readLock.unlock();
        }
    }

    public void updateSalary(int empId, double newSalary) throws EmployeeNotFoundException, InvalidEmployeeDataException {
        writeLock.lock();
        try {
            if (!ValidationUtil.isPositive(newSalary)) {
                throw new InvalidEmployeeDataException("Salary must be positive.");
            }

            Employee employee = employeeMap.get(empId);

            if (employee == null) {
                throw new EmployeeNotFoundException(empId);
            }

            employee.setSalary(newSalary);
        } finally {
            writeLock.unlock();
        }
    }

    public int getTotalEmployees() {
        readLock.lock();
        try {
            return employees.size();
        } finally {
            readLock.unlock();
        }
    }

    public void findHighestPaidEmployee() {
        readLock.lock();
        try {
            if (employees.isEmpty()) {
                System.out.println("No employees found!");
                return;
            }

            Employee highestPaidEmployee = employees.get(0);

            for (Employee employee : employees) {
                if (employee.getSalary() > highestPaidEmployee.getSalary()) {
                    highestPaidEmployee = employee;
                }
            }

            System.out.println("Highest Paid Employee:");
            System.out.println(highestPaidEmployee);
        } finally {
            readLock.unlock();
        }
    }

    public void displayEmployeesInSortedOrder() {
        readLock.lock();
        try {
            if (employees.isEmpty()) {
                System.out.println("No employees in the system!");
                return;
            }

            Set<Employee> sortedEmployees = new TreeSet<>(
                    Comparator.comparingInt(Employee::getEmpId)
                            .thenComparing(Employee::getName, String.CASE_INSENSITIVE_ORDER)
            );
            sortedEmployees.addAll(employees);

            System.out.println();
            System.out.println(FormatUtil.line(80, "="));
            System.out.println("EMPLOYEES IN SORTED ORDER");
            System.out.println(FormatUtil.line(80, "="));

            for (Employee employee : sortedEmployees) {
                System.out.println(employee);
            }

            System.out.println(FormatUtil.line(80, "="));
        } finally {
            readLock.unlock();
        }
    }

    public void saveEmployeesToFile(String fileName) throws IOException {
        Path filePath = Path.of(fileName);
        Path parentPath = filePath.getParent();

        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }

        List<String> records = new ArrayList<>();

        readLock.lock();
        try {
            for (Employee employee : employees) {
                records.add(toFileRecord(employee));
            }
        } finally {
            readLock.unlock();
        }

        Files.write(filePath, records, StandardCharsets.UTF_8);
    }

    public void loadEmployeesFromFile(String fileName) throws IOException {
        Path filePath = Path.of(fileName);

        if (!Files.exists(filePath)) {
            return;
        }

        List<String> records = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        // Hold the write lock while rebuilding the entire in-memory state so readers never see a half-loaded dataset.
        // The lock is reentrant, so reusing addEmployee() here is safe even though this method already owns the write lock.
        writeLock.lock();
        try {
            employees.clear();
            employeeMap.clear();
            departments.clear();

            for (int index = 0; index < records.size(); index++) {
                String record = records.get(index);

                if (record.trim().isEmpty()) {
                    continue;
                }

                Employee employee = fromFileRecord(record, index + 1);

                try {
                    addEmployee(employee);
                } catch (DuplicateEmployeeException | InvalidEmployeeDataException exception) {
                    throw new IOException("Invalid employee data at line " + (index + 1) + ": " + exception.getMessage(), exception);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void rebuildDepartments() {
        departments.clear();

        for (Employee employee : employees) {
            departments.add(employee.getDepartment());
        }
    }

    private String toFileRecord(Employee employee) {
        if (employee instanceof PermanentEmployee permanentEmployee) {
            return String.join("|",
                    "PERMANENT",
                    String.valueOf(employee.getEmpId()),
                    encode(employee.getName()),
                    encode(employee.getDepartment()),
                    String.valueOf(employee.getSalary()),
                    String.valueOf(permanentEmployee.getYearsOfService()));
        }

        if (employee instanceof ContractEmployee contractEmployee) {
            return String.join("|",
                    "CONTRACT",
                    String.valueOf(employee.getEmpId()),
                    encode(employee.getName()),
                    encode(employee.getDepartment()),
                    String.valueOf(employee.getSalary()),
                    String.valueOf(contractEmployee.getContractMonths()));
        }

        if (employee instanceof Intern intern) {
            return String.join("|",
                    "INTERN",
                    String.valueOf(employee.getEmpId()),
                    encode(employee.getName()),
                    encode(employee.getDepartment()),
                    String.valueOf(employee.getSalary()),
                    encode(intern.getUniversity()));
        }

        throw new IllegalArgumentException("Unsupported employee type: " + employee.getClass().getSimpleName());
    }

    private Employee fromFileRecord(String record, int lineNumber) throws IOException {
        String[] parts = record.split("\\|", -1);

        if (parts.length != 6) {
            throw new IOException("Invalid employee file format at line " + lineNumber + ".");
        }

        try {
            String type = parts[0];
            int empId = Integer.parseInt(parts[1]);
            String name = decode(parts[2]);
            String department = decode(parts[3]);
            double salary = Double.parseDouble(parts[4]);

            switch (type) {
                case "PERMANENT":
                    return new PermanentEmployee(empId, name, department, salary, Integer.parseInt(parts[5]));
                case "CONTRACT":
                    return new ContractEmployee(empId, name, department, salary, Integer.parseInt(parts[5]));
                case "INTERN":
                    return new Intern(empId, name, department, salary, decode(parts[5]));
                default:
                    throw new IOException("Unknown employee type at line " + lineNumber + ": " + type);
            }
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid employee file data at line " + lineNumber + ".", exception);
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
