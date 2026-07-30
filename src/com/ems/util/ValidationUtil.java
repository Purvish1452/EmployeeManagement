package com.ems.util;

import com.ems.model.Employee;

public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static boolean isPositive(int value) {
        return value > 0;
    }

    public static boolean isPositive(double value) {
        return value > 0;
    }

    public static boolean isNonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static void validateEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null.");
        }

        if (!isPositive(employee.getEmpId())) {
            throw new IllegalArgumentException("Employee ID must be positive.");
        }

        if (!isNonBlank(employee.getName())) {
            throw new IllegalArgumentException("Employee name cannot be empty.");
        }

        if (!isNonBlank(employee.getDepartment())) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }

        if (!isPositive(employee.getSalary())) {
            throw new IllegalArgumentException("Salary must be positive.");
        }
    }
}
