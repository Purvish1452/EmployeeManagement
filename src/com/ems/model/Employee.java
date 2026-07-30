package com.ems.model;

import com.ems.util.FormatUtil;

public abstract class Employee {
    private final int empId;
    private final String name;
    private final String department;
    private final double salary;

    public Employee(int empId, String name, String department, double salary) {
        this.empId = empId;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public abstract double calculateBonus();

    /** Returns an immutable copy of this employee with the supplied salary. */
    public abstract Employee withSalary(double salary);

    public int getEmpId() {
        return empId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public double getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return "ID: " + empId
                + " | Name: " + name
                + " | Department: " + department
                + " | Salary: " + FormatUtil.currency(getSalary())
                + " | Bonus: " + FormatUtil.currency(calculateBonus());
    }
}
