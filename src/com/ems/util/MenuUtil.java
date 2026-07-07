package com.ems.util;

import com.ems.enums.EmployeeType;

public final class MenuUtil {
    private MenuUtil() {
    }

    public static void displayWelcome() {
        System.out.println();
        System.out.println(FormatUtil.line(40, "="));
        System.out.println("        EMPLOYEE MANAGEMENT SYSTEM       ");
        System.out.println("          Using Collections and OOP      ");
        System.out.println(FormatUtil.line(40, "="));
    }

    public static void displayMainMenu() {
        System.out.println();
        System.out.println(FormatUtil.line(40, "-"));
        System.out.println("MAIN MENU");
        System.out.println(FormatUtil.line(40, "-"));
        System.out.println("1. Add New Employee");
        System.out.println("2. Remove Employee");
        System.out.println("3. Search Employee");
        System.out.println("4. Display All Employees");
        System.out.println("5. Display Employees by Department");
        System.out.println("6. Display All Departments");
        System.out.println("7. Update Employee Salary");
        System.out.println("8. Calculate Total Payroll");
        System.out.println("9. Find Highest Paid Employee");
        System.out.println("10. Exit");
        System.out.println("11. Display Employees in Sorted Order");
        System.out.println(FormatUtil.line(40, "-"));
    }

    public static void displayEmployeeTypeMenu() {
        System.out.println();
        System.out.println("Employee Type:");

        for (EmployeeType employeeType : EmployeeType.values()) {
            System.out.println(employeeType.getMenuChoice() + ". " + employeeType.getDisplayName());
        }
    }
}
