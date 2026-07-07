package com.ems.util;

import java.util.Scanner;

public class InputUtil {
    private final Scanner scanner;

    public InputUtil(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readPositiveInt(String prompt) {
        while (true) {
            var value = readInt(prompt); // var is clear because readInt returns an int.

            if (ValidationUtil.isPositive(value)) {
                return value;
            }

            System.out.println("Please enter a positive number.");
        }
    }

    public double readPositiveDouble(String prompt) {
        while (true) {
            var value = readDouble(prompt); // var is clear because readDouble returns a double.

            if (ValidationUtil.isPositive(value)) {
                return value;
            }

            System.out.println("Please enter a positive amount.");
        }
    }

    public String readNonBlankString(String prompt) {
        while (true) {
            System.out.print(prompt);
            var value = scanner.nextLine().trim(); // var is clear because nextLine().trim() returns a String.

            if (ValidationUtil.isNonBlank(value)) {
                return value;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private int readInt(String prompt) {
        System.out.print(prompt);

        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input! Please enter a valid number.");
            System.out.print(prompt);
            scanner.nextLine();
        }

        var value = scanner.nextInt(); // var is clear because nextInt returns an int.
        scanner.nextLine();
        return value;
    }

    private double readDouble(String prompt) {
        System.out.print(prompt);

        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input! Please enter a valid number.");
            System.out.print(prompt);
            scanner.nextLine();
        }

        var value = scanner.nextDouble(); // var is clear because nextDouble returns a double.
        scanner.nextLine();
        return value;
    }
}
