package com.ems.exception;

public class InvalidEmployeeTypeException extends Exception {
    public InvalidEmployeeTypeException(int choice) {
        super("Invalid employee type selected: " + choice);
    }
}
