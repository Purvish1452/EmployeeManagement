package com.ems.model;

public class Intern extends Employee {
    private String university;

    public Intern(int empId, String name, String department, double salary, String university) {
        super(empId, name, department, salary);
        this.university = university;
    }

    @Override
    public double calculateBonus() {
        return salary * 0.02;
    }

    public String getUniversity() {
        return university;
    }
}
