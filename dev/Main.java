import dataaccess.DatabaseInitializer;
import dataaccess.DatabaseSeeder;
import employee.presentation.ConsolePresentation;
import transportation.presentation.DeliveriesUI;
import transportation.service.DeliveriesApplication;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initializeDatabase();
            DatabaseSeeder.seedSampleData();
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Select subsystem:");
        System.out.println("1. Employees");
        System.out.println("2. Deliveries");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                new ConsolePresentation().run();
                break;
            case "2":
                new DeliveriesUI(new DeliveriesApplication()).start();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
}