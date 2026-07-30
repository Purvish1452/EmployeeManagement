package com.ems.model;

public class ContractEmployee extends Employee {
    private final int contractMonths;

    public ContractEmployee(int empId, String name, String department, double salary, int contractMonths) {
        super(empId, name, department, salary);
        this.contractMonths = contractMonths;
    }

    @Override
    public double calculateBonus() {
        return getSalary() * 0.05;
    }

    @Override
    public ContractEmployee withSalary(double salary) {
        return new ContractEmployee(getEmpId(), getName(), getDepartment(), salary, contractMonths);
    }

    public int getContractMonths() {
        return contractMonths;
    }

}
