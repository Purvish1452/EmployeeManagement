package com.ems.exception;

public class EmployeeNotFoundException extends Exception {
    public EmployeeNotFoundException(int empId) {
        super("Employee with ID " + empId + " was not found.");
    }
}
