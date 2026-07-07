package com.ems.exception;

public class DuplicateEmployeeException extends Exception {
    public DuplicateEmployeeException(int empId) {
        super("Employee with ID " + empId + " already exists.");
    }
}
