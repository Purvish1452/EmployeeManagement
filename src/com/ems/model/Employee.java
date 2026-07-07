package com.ems.model;

import com.ems.util.FormatUtil;

public abstract class Employee {
    protected int empId;
    protected String name;
    protected String department;
    protected double salary;

    public Employee(int empId, String name, String department, double salary) {
        this.empId = empId;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public abstract double calculateBonus();

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

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "ID: " + empId
                + " | Name: " + name
                + " | Department: " + department
                + " | Salary: " + FormatUtil.currency(salary)
                + " | Bonus: " + FormatUtil.currency(calculateBonus());
    }
}
