package com.ems.service;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe in-memory employee store for one authenticated user.
 * A single lock protects all related indexes, so standard collections are faster and sufficient.
 */
public class EmployeeManager {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Integer, Employee> employeesById = new LinkedHashMap<>();
    private final Set<String> departments = new HashSet<>();
    private int nextEmployeeId = 101;


    public void addEmployee(Employee employee) {
        ValidationUtil.validateEmployee(employee);
        lock.writeLock().lock();
        try {
            addEmployeeLocked(employee);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeEmployee(int empId) {
        lock.writeLock().lock();
        try {
            if (employeesById.remove(empId) == null) {
                throw employeeNotFound(empId);
            }
            rebuildDepartmentsLocked();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Employee searchEmployee(int empId) {
        lock.readLock().lock();
        try {
            Employee employee = employeesById.get(empId);
            if (employee == null) {
                throw employeeNotFound(empId);
            }
            return employee;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Employee> getEmployees() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(employeesById.values()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        if (!ValidationUtil.isNonBlank(department)) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }
        lock.readLock().lock();
        try {
            List<Employee> matches = new ArrayList<>();
            for (Employee employee : employeesById.values()) {
                if (employee.getDepartment().equalsIgnoreCase(department)) {
                    matches.add(employee);
                }
            }
            if (matches.isEmpty()) {
                throw new NoSuchElementException("No employees found in " + department + " department.");
            }
            return Collections.unmodifiableList(matches);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<String> getDepartments() {
        lock.readLock().lock();
        try {
            Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            result.addAll(departments);
            return Collections.unmodifiableSet(result);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateSalary(int empId, double newSalary) {
        if (!ValidationUtil.isPositive(newSalary)) {
            throw new IllegalArgumentException("Salary must be positive.");
        }
        lock.writeLock().lock();
        try {
            Employee employee = employeesById.get(empId);
            if (employee == null) {
                throw employeeNotFound(empId);
            }
            employeesById.put(empId, employee.withSalary(newSalary));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getTotalEmployees() {
        lock.readLock().lock();
        try {
            return employeesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int addPermanentEmployeeWithNextId(String name, String department, double salary) {
        lock.writeLock().lock();
        try {
            int employeeId = nextEmployeeId;
            addEmployeeLocked(new PermanentEmployee(employeeId, name, department, salary, 1));
            return employeeId;

        } finally {
            lock.writeLock().unlock();
        }
    }

    public Employee getHighestPaidEmployee() {
        lock.readLock().lock();
        try {
            Employee highestPaidEmployee = null;

            for (Employee employee : employeesById.values()) {
                if (highestPaidEmployee == null
                        || employee.getSalary() > highestPaidEmployee.getSalary()) {
                    highestPaidEmployee = employee;
                }
            }

            if (highestPaidEmployee == null) {
                throw new NoSuchElementException("No employees found.");
            }

            return highestPaidEmployee;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Employee> getEmployeesSortedById() {
        List<Employee> result = new ArrayList<>(getEmployees());
        Collections.sort(result, new Comparator<Employee>() {
            @Override
            public int compare(Employee firstEmployee, Employee secondEmployee) {
                int idComparison = Integer.compare(firstEmployee.getEmpId(), secondEmployee.getEmpId());

                if (idComparison != 0) {
                    return idComparison;
                }

                return String.CASE_INSENSITIVE_ORDER.compare(
                        firstEmployee.getName(), secondEmployee.getName());
            }
        });
        return Collections.unmodifiableList(result);
    }

    public PayrollSummary getPayrollSummary() {
        List<Employee> snapshot = getEmployees();
        double salary = PayrollUtil.calculateTotalSalary(snapshot);
        double bonus = PayrollUtil.calculateTotalBonus(snapshot);
        return new PayrollSummary(snapshot.size(), salary, bonus);
    }

    public void saveEmployeesToFile(Path filePath) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<String> records = new ArrayList<>();
        for (Employee employee : getEmployees()) {
            records.add(toFileRecord(employee));
        }
        Files.write(filePath, records, StandardCharsets.UTF_8);
    }

    public void loadEmployeesFromFile(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            return;
        }
        List<String> records = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<Employee> loadedEmployees = new ArrayList<>();
        for (int index = 0; index < records.size(); index++) {
            String record = records.get(index).trim();
            if (!record.isEmpty()) {
                loadedEmployees.add(fromFileRecord(record, index + 1));
            }
        }
        lock.writeLock().lock();
        try {
            employeesById.clear();
            departments.clear();
            nextEmployeeId = 101;
            for (Employee employee : loadedEmployees) {
                addEmployeeLocked(employee);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String formatEmployees(List<Employee> employeeList, String title) {
        if (employeeList.isEmpty()) {
            return "No employees in the system.";
        }
        StringBuilder output = new StringBuilder().append(FormatUtil.line(80, "=")).append(System.lineSeparator())
                .append(title).append(System.lineSeparator()).append(FormatUtil.line(80, "=")).append(System.lineSeparator());
        for (Employee employee : employeeList) {
            output.append(employee).append(System.lineSeparator());
        }
        return output.append(FormatUtil.line(80, "=")).toString();
    }

    private void
    addEmployeeLocked(Employee employee) {
        ValidationUtil.validateEmployee(employee);
        if (employeesById.containsKey(employee.getEmpId())) {
            throw new IllegalStateException("Employee with ID " + employee.getEmpId() + " already exists.");
        }
        employeesById.put(employee.getEmpId(), employee);
        departments.add(employee.getDepartment());
        nextEmployeeId = Math.max(nextEmployeeId, employee.getEmpId() + 1);
    }

    private void rebuildDepartmentsLocked() {
        departments.clear();
        for (Employee employee : employeesById.values()) {
            departments.add(employee.getDepartment());
        }
    }

    private NoSuchElementException employeeNotFound(int empId) {
        return new NoSuchElementException("Employee with ID " + empId + " was not found.");
    }

    private String toFileRecord(Employee employee) {
        if (employee instanceof PermanentEmployee) {
            PermanentEmployee permanent = (PermanentEmployee) employee;
            return String.join("|", "PERMANENT", String.valueOf(employee.getEmpId()), encode(employee.getName()), encode(employee.getDepartment()), String.valueOf(employee.getSalary()), String.valueOf(permanent.getYearsOfService()));
        }
        if (employee instanceof ContractEmployee) {
            ContractEmployee contract = (ContractEmployee) employee;
            return String.join("|", "CONTRACT", String.valueOf(employee.getEmpId()), encode(employee.getName()), encode(employee.getDepartment()), String.valueOf(employee.getSalary()), String.valueOf(contract.getContractMonths()));
        }
        if (employee instanceof Intern) {
            Intern intern = (Intern) employee;
            return String.join("|", "INTERN", String.valueOf(employee.getEmpId()), encode(employee.getName()), encode(employee.getDepartment()), String.valueOf(employee.getSalary()), encode(intern.getUniversity()));
        }
        throw new IllegalArgumentException("Unsupported employee type: " + employee.getClass().getSimpleName());
    }

    private Employee fromFileRecord(String record, int lineNumber) throws IOException {
        String[] parts = record.split("\\|", -1);
        if (parts.length != 6) {
            throw new IOException("Invalid employee file format at line " + lineNumber + ".");
        }
        try {
            int id = Integer.parseInt(parts[1]);
            String name = decode(parts[2]);
            String department = decode(parts[3]);
            double salary = Double.parseDouble(parts[4]);
            if ("PERMANENT".equals(parts[0])) return new PermanentEmployee(id, name, department, salary, Integer.parseInt(parts[5]));
            if ("CONTRACT".equals(parts[0])) return new ContractEmployee(id, name, department, salary, Integer.parseInt(parts[5]));
            if ("INTERN".equals(parts[0])) return new Intern(id, name, department, salary, decode(parts[5]));
            throw new IllegalArgumentException("Unknown employee type '" + parts[0] + "'.");
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid employee file data at line " + lineNumber + ".", exception);
        }
    }

    private String encode(String value) { return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    private String decode(String value) { return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8); }

    public static final class PayrollSummary {
        private final int employeeCount;
        private final double totalSalary;
        private final double totalBonus;
        public PayrollSummary(int employeeCount, double totalSalary, double totalBonus) { this.employeeCount = employeeCount; this.totalSalary = totalSalary; this.totalBonus = totalBonus; }
        public int getEmployeeCount() { return employeeCount; }
        public double getTotalSalary() { return totalSalary; }
        public double getTotalBonus() { return totalBonus; }
        public double getTotalPayroll() { return totalSalary + totalBonus; }
    }
}
