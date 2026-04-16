package dev;

import employees.domain.HR_Manager;
import employees.domain.User;
import employees.service.AuthenticationService;
import employees.repository.impl.FileUserRepository;

import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AuthenticationService authenticationService = new AuthenticationService(new FileUserRepository());
        try {
            authenticationService.registerUser(new User("employee1", "pass123"));
            authenticationService.registerUser(new HR_Manager("hr001", "hrpass"));
        } catch (Exception e) {
            System.out.println("Failed to register demo users: " + e.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("User id: ");
            String id = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            Optional<User> loggedInUser = authenticationService.login(id, password);
            if (!loggedInUser.isPresent()) {
                System.out.println("Invalid credentials.");
                return;
            }

            System.out.println("Login successful. Welcome " + loggedInUser.get().getId() + ".");

            boolean running = true;
            while (running) {
                System.out.println("\nChoose action:");
                System.out.println("1. Logout");
                System.out.println("2. Exit");
                System.out.print("Selection: ");

                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        if (authenticationService.logout()) {
                            System.out.println("You have been logged out.");
                        } else {
                            System.out.println("No user is currently logged in.");
                        }
                        running = false;
                        break;
                    case "2":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid selection.");
                }
            }
        }
    }
}