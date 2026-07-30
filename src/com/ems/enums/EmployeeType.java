package com.ems.enums;

public enum EmployeeType {
    PERMANENT(1, "Permanent Employee"),
    CONTRACT(2, "Contract Employee"),
    INTERN(3, "Intern");

    private final int menuChoice;
    private final String displayName;

    EmployeeType(int menuChoice, String displayName) {
        this.menuChoice = menuChoice;
        this.displayName = displayName;
    }

    public int getMenuChoice() {
        return menuChoice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EmployeeType fromMenuChoice(int menuChoice) {
        for (EmployeeType employeeType : values()) {
            if (employeeType.menuChoice == menuChoice) {
                return employeeType;
            }
        }

        throw new IllegalArgumentException("Invalid employee type selected: " + menuChoice);
    }
}
