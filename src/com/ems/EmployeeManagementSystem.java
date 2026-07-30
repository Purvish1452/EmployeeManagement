package com.ems;

import com.ems.enums.EmployeeType;
import com.ems.model.ContractEmployee;
import com.ems.model.Employee;
import com.ems.model.Intern;
import com.ems.model.PermanentEmployee;
import com.ems.service.EmployeeManager;
import com.ems.service.UserAuthenticationService;
import com.ems.util.FormatUtil;
import com.ems.util.InputUtil;
import com.ems.util.MenuUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Standalone console application. Every run authenticates before accessing employee records. */
public class EmployeeManagementSystem {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final InputUtil INPUT = new InputUtil(SCANNER);
    private static final UserAuthenticationService AUTHENTICATION = new UserAuthenticationService();
    private static UserAuthenticationService.UserSession session;

    public static void main(String[] args) {
        session = authenticate();
        if (session == null) return;
        MenuUtil.displayWelcome();
        System.out.println("Signed in as " + session.getUsername() + ".");
        while (true) {
            MenuUtil.displayMainMenu();
            try {
                switch (INPUT.readPositiveInt("Enter your choice: ")) {
                    case 1: addEmployee(); break;
                    case 2: removeEmployee(); break;
                    case 3: searchEmployee(); break;
                    case 4: displayEmployees(); break;
                    case 5: displayByDepartment(); break;
                    case 6: displayDepartments(); break;
                    case 7: updateSalary(); break;
                    case 8: displayPayroll(); break;
                    case 9: System.out.println(manager().getHighestPaidEmployee()); break;
                    case 10: saveAndExit(); return;
                    case 11: System.out.println(manager().formatEmployees(manager().getEmployeesSortedById(), "EMPLOYEES IN SORTED ORDER")); break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException | IOException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private static UserAuthenticationService.UserSession authenticate() {
        try {
            String mode = INPUT.readNonBlankString("Log in (L) or register (R): ");
            String username = INPUT.readNonBlankString("Username: ");
            System.out.print("Password: ");
            char[] password = SCANNER.nextLine().toCharArray();
            try {
                return mode.equalsIgnoreCase("R") ? AUTHENTICATION.register(username, password) : AUTHENTICATION.login(username, password);
            } finally { Arrays.fill(password, '\0'); }
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            System.out.println("Unable to authenticate: " + exception.getMessage());
            return null;
        }
    }

    private static void addEmployee() throws IOException {
        int id = INPUT.readPositiveInt("Employee ID: ");
        String name = INPUT.readNonBlankString("Name: ");
        String department = INPUT.readNonBlankString("Department: ");
        double salary = INPUT.readPositiveDouble("Salary: ");
        MenuUtil.displayEmployeeTypeMenu();
        EmployeeType type = EmployeeType.fromMenuChoice(INPUT.readPositiveInt("Select type (1-3): "));
        Employee employee;
        switch (type) {
            case PERMANENT: employee = new PermanentEmployee(id, name, department, salary, INPUT.readPositiveInt("Years of service: ")); break;
            case CONTRACT: employee = new ContractEmployee(id, name, department, salary, INPUT.readPositiveInt("Contract months: ")); break;
            case INTERN: employee = new Intern(id, name, department, salary, INPUT.readNonBlankString("University: ")); break;
            default: throw new IllegalStateException("Unsupported employee type.");
        }
        manager().addEmployee(employee);
        session.saveEmployees();
        System.out.println("Employee added successfully.");
    }

    private static void removeEmployee() throws IOException { manager().removeEmployee(INPUT.readPositiveInt("Employee ID to remove: ")); session.saveEmployees(); System.out.println("Employee removed successfully."); }
    private static void searchEmployee() { System.out.println(manager().searchEmployee(INPUT.readPositiveInt("Employee ID to search: "))); }
    private static void displayEmployees() { System.out.println(manager().formatEmployees(manager().getEmployees(), "ALL EMPLOYEES")); }
    private static void displayByDepartment() { String department = INPUT.readNonBlankString("Department: "); System.out.println(manager().formatEmployees(manager().getEmployeesByDepartment(department), "EMPLOYEES IN " + department.toUpperCase() + " DEPARTMENT")); }
    private static void displayDepartments() { System.out.println(manager().getDepartments().isEmpty() ? "No departments found." : "All Departments: " + manager().getDepartments()); }
    private static void updateSalary() throws IOException { manager().updateSalary(INPUT.readPositiveInt("Employee ID: "), INPUT.readPositiveDouble("New salary: ")); session.saveEmployees(); System.out.println("Salary updated successfully."); }
    private static void displayPayroll() { EmployeeManager.PayrollSummary p = manager().getPayrollSummary(); System.out.println("Total Employees: " + p.getEmployeeCount() + "\nTotal Salary: " + FormatUtil.currency(p.getTotalSalary()) + "\nTotal Bonus: " + FormatUtil.currency(p.getTotalBonus()) + "\nTotal Payroll: " + FormatUtil.currency(p.getTotalPayroll())); }
    private static void saveAndExit() { try { session.saveEmployees(); System.out.println("Data saved. Goodbye!"); } catch (IOException exception) { System.out.println("Unable to save employee data: " + exception.getMessage()); } finally { SCANNER.close(); } }
    private static EmployeeManager manager() { return session.getEmployeeManager(); }
}
