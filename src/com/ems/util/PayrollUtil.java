package com.ems.util;

import com.ems.model.Employee;

import java.util.List;

public final class PayrollUtil {
    private PayrollUtil() {
    }

    public static double calculateTotalSalary(List<Employee> employees) {
        var totalSalary = 0.0; // var is clear because the decimal literal infers double.

        for (Employee employee : employees) {
            totalSalary += employee.getSalary();
        }

        return totalSalary;
    }

    public static double calculateTotalBonus(List<Employee> employees) {
        var totalBonus = 0.0; // var is clear because the decimal literal infers double.

        for (Employee employee : employees) {
            totalBonus += employee.calculateBonus();
        }

        return totalBonus;
    }
}
