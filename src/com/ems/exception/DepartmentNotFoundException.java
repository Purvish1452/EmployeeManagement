package com.ems.exception;

public class DepartmentNotFoundException extends Exception {
    public DepartmentNotFoundException(String department) {
        super("No employees found in " + department + " department.");
    }
}
