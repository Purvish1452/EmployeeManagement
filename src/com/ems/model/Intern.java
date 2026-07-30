package com.ems.model;

public class Intern extends Employee {
    private final String university;

    public Intern(int empId, String name, String department, double salary, String university) {
        super(empId, name, department, salary);
        this.university = university;
    }

    @Override
    public double calculateBonus() {
        return getSalary() * 0.02;
    }

    @Override
    public Intern withSalary(double salary) {
        return new Intern(getEmpId(), getName(), getDepartment(), salary, university);
    }

    public String getUniversity() {
        return university;
    }
}
