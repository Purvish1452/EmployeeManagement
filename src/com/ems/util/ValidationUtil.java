package com.ems.util;

import com.ems.exception.InvalidEmployeeDataException;
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

    public static void validateEmployee(Employee employee) throws InvalidEmployeeDataException {
        if (employee == null) {
            throw new InvalidEmployeeDataException("Employee cannot be null.");
        }

        if (!isPositive(employee.getEmpId())) {
            throw new InvalidEmployeeDataException("Employee ID must be positive.");
        }

        if (!isNonBlank(employee.getName())) {
            throw new InvalidEmployeeDataException("Employee name cannot be empty.");
        }

        if (!isNonBlank(employee.getDepartment())) {
            throw new InvalidEmployeeDataException("Department cannot be empty.");
        }

        if (!isPositive(employee.getSalary())) {
            throw new InvalidEmployeeDataException("Salary must be positive.");
        }
    }
}
