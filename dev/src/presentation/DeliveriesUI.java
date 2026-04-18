package presentation;

import java.util.Scanner;
import service.DeliveriesApplication;

public class DeliveriesUI {
    private DeliveriesApplication deliveriesApplication;
    private Scanner scanner;

    public DeliveriesUI() {
        this.deliveriesApplication = new DeliveriesApplication();
        this.scanner = new Scanner(System.in);
    }

    public DeliveriesApplication getDeliveriesApplication() {
        return this.deliveriesApplication;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("=== Deliveries System ===");
            System.out.println("1. Show main menu");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Main menu selected.");
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting system...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

            System.out.println();
        }
    }
}