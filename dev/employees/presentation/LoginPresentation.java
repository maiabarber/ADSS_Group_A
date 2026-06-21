package employees.presentation;

import java.util.Scanner;

/**
 * LoginPresentation class provides a presentation layer for handling user login input.
 * It contains fields for user ID and password input, and a method to read these inputs from the console.
 */
public class LoginPresentation {
    private String idInput;
    private String passwordInput;

    public void readLoginInput(Scanner scanner) {
        System.out.print("User id: ");
        idInput = scanner.nextLine();
        System.out.print("Password: ");
        passwordInput = scanner.nextLine();
    }

    public String getIdInput() {
        return idInput;
    }

    public String getPasswordInput() {
        return passwordInput;
    }
}
