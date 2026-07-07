package com.ems;

import com.ems.enums.EmployeeType;
import com.ems.exception.DepartmentNotFoundException;
import com.ems.exception.DuplicateEmployeeException;
import com.ems.exception.EmployeeNotFoundException;
import com.ems.exception.InvalidEmployeeDataException;
import com.ems.exception.InvalidEmployeeTypeException;
import com.ems.model.ContractEmployee;
import com.ems.model.Employee;
import com.ems.model.Intern;
import com.ems.model.PermanentEmployee;
import com.ems.service.EmployeeManager;
import com.ems.util.InputUtil;
import com.ems.util.MenuUtil;

import java.io.IOException;
import java.util.Scanner;

public class EmployeeManagementSystem {
    private static final String DATA_FILE = "data/employees.txt";
    private static final EmployeeManager manager = new EmployeeManager();
    private static final Scanner scanner = new Scanner(System.in);
    private static final InputUtil inputUtil = new InputUtil(scanner);

    public static void main(String[] args) {
        loadSavedEmployees();
        MenuUtil.displayWelcome();

        while (true) {
            MenuUtil.displayMainMenu();
            var choice = inputUtil.readPositiveInt("Enter your choice: "); // var is clear because readPositiveInt returns an int menu choice.

            switch (choice) {
                case 1:
                    addNewEmployee();
                    break;
                case 2:
                    removeEmployee();
                    break;
                case 3:
                    searchEmployee();
                    break;
                case 4:
                    manager.displayAllEmployees();
                    break;
                case 5:
                    displayByDepartment();
                    break;
                case 6:
                    manager.displayAllDepartments();
                    break;
                case 7:
                    updateEmployeeSalary();
                    break;
                case 8:
                    manager.calculateTotalPayroll();
                    break;
                case 9:
                    manager.findHighestPaidEmployee();
                    break;
                case 10:
                    saveAllData();
                    System.out.println("Exiting the system. Goodbye!");
                    scanner.close();
                    return;
                case 11:
                    manager.displayEmployeesInSortedOrder();
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void addNewEmployee() {
        System.out.println();
        System.out.println("--- ADD NEW EMPLOYEE ---");

        var empId = inputUtil.readPositiveInt("Enter Employee ID: "); // var is clear because readPositiveInt returns an int ID.
        var name = inputUtil.readNonBlankString("Enter Name: "); // var is clear because readNonBlankString returns String input.
        var department = inputUtil.readNonBlankString("Enter Department: "); // var is clear because readNonBlankString returns String input.
        var salary = inputUtil.readPositiveDouble("Enter Salary: "); // var is clear because readPositiveDouble returns a double amount.

        MenuUtil.displayEmployeeTypeMenu();

        var typeChoice = inputUtil.readPositiveInt("Select type (1-3): "); // var is clear because readPositiveInt returns an int selection.

        try {
            EmployeeType employeeType = EmployeeType.fromMenuChoice(typeChoice);

            switch (employeeType) {
                case PERMANENT:
                    var years = inputUtil.readPositiveInt("Enter Years of Service: "); // var is clear because readPositiveInt returns an int count.
                    manager.addEmployee(new PermanentEmployee(empId, name, department, salary, years));
                    break;
                case CONTRACT:
                    var months = inputUtil.readPositiveInt("Enter Contract Months: "); // var is clear because readPositiveInt returns an int count.
                    manager.addEmployee(new ContractEmployee(empId, name, department, salary, months));
                    break;
                case INTERN:
                    var university = inputUtil.readNonBlankString("Enter University: "); // var is clear because readNonBlankString returns String input.
                    manager.addEmployee(new Intern(empId, name, department, salary, university));
                    break;
            }

            manager.saveEmployeesToFile(DATA_FILE);
            System.out.println("Employee added successfully!");
        } catch (DuplicateEmployeeException | InvalidEmployeeDataException | InvalidEmployeeTypeException | IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void removeEmployee() {
        var empId = inputUtil.readPositiveInt("Enter Employee ID to remove: "); // var is clear because readPositiveInt returns an int ID.

        try {
            manager.removeEmployee(empId);
            manager.saveEmployeesToFile(DATA_FILE);
            System.out.println("Employee removed successfully!");
        } catch (EmployeeNotFoundException | IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void searchEmployee() {
        var empId = inputUtil.readPositiveInt("Enter Employee ID to search: "); // var is clear because readPositiveInt returns an int ID.

        try {
            Employee employee = manager.searchEmployee(empId);
            System.out.println("Employee Found:");
            System.out.println(employee);
        } catch (EmployeeNotFoundException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void displayByDepartment() {
        var department = inputUtil.readNonBlankString("Enter Department Name: "); // var is clear because readNonBlankString returns String input.

        try {
            manager.displayByDepartment(department);
        } catch (DepartmentNotFoundException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void updateEmployeeSalary() {
        var empId = inputUtil.readPositiveInt("Enter Employee ID: "); // var is clear because readPositiveInt returns an int ID.
        var newSalary = inputUtil.readPositiveDouble("Enter New Salary: "); // var is clear because readPositiveDouble returns a double amount.

        try {
            manager.updateSalary(empId, newSalary);
            manager.saveEmployeesToFile(DATA_FILE);
            System.out.println("Salary updated successfully!");
        } catch (EmployeeNotFoundException | InvalidEmployeeDataException | IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void loadSavedEmployees() {
        try {
            manager.loadEmployeesFromFile(DATA_FILE);
        } catch (IOException exception) {
            System.out.println("Unable to load saved employee data: " + exception.getMessage());
        }
    }

    private static void saveAllData() {
        try {
            manager.saveEmployeesToFile(DATA_FILE);
        } catch (IOException exception) {
            System.out.println("Unable to save employee data: " + exception.getMessage());
        }
    }
}
