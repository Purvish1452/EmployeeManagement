package com.ems.model;

public class PermanentEmployee extends Employee {
    private int yearsOfService;

    public PermanentEmployee(int empId, String name, String department, double salary, int yearsOfService) {
        super(empId, name, department, salary);
        this.yearsOfService = yearsOfService;
    }

    @Override
    public double calculateBonus() {
        return salary * 0.10 * (yearsOfService / 5);
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

    public void setYearsOfService(int yearsOfService) {
        this.yearsOfService = yearsOfService;
    }
}
