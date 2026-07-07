package com.ems.model;

public class ContractEmployee extends Employee {
    private int contractMonths;

    public ContractEmployee(int empId, String name, String department, double salary, int contractMonths) {
        super(empId, name, department, salary);
        this.contractMonths = contractMonths;
    }

    @Override
    public double calculateBonus() {
        return salary * 0.05;
    }

    public int getContractMonths() {
        return contractMonths;
    }

    public void setContractMonths(int contractMonths) {
        this.contractMonths = contractMonths;
    }
}
