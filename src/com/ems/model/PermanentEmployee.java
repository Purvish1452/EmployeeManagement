package com.ems.model;

public class PermanentEmployee extends Employee {
    private final int yearsOfService;

    public PermanentEmployee(int empId, String name, String department, double salary, int yearsOfService) {
        super(empId, name, department, salary);
        this.yearsOfService = yearsOfService;
    }

    @Override
    public double calculateBonus() {
        return getSalary() * 0.10 * (yearsOfService / 5);
    }

    @Override
    public PermanentEmployee withSalary(double salary) {
        return new PermanentEmployee(getEmpId(), getName(), getDepartment(), salary, yearsOfService);
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

}
