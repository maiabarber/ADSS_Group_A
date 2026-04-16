package employees.presentation;

import java.util.Scanner;

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
