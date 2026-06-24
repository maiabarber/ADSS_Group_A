package employee.presentation;

import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Role;
import employee.domain.BranchManager;
import employee.service.HRManagerBranchService;
import employee.repository.EmployeeRepository;
import employee.repository.ShiftRepository;
import java.util.List;
import java.util.Scanner;

/**
 * HRManagerPresentation handles HR Manager operations for managing their branch.
 * HR Managers can:
 * - View their assigned branch
 * - View employees in their branch
 * - Assign shifts to branch employees
 * - Manage shift swaps within their branch
 */
public class HRManagerPresentation {

    /**
     * HR Manager selects their branch and gets a service to manage it
     */
    public HRManagerBranchService selectManagedBranch(Scanner scanner, BranchManager branchManager,
                                                       EmployeeRepository employeeRepository,
                                                       ShiftRepository shiftRepository) {
        List<Branch> allBranches = branchManager.getAllBranches();
        
        if (allBranches.isEmpty()) {
            System.out.println("No branches available in the system.");
            return null;
        }
        
        System.out.println("\nAvailable Branches:");
        for (int i = 0; i < allBranches.size(); i++) {
            Branch branch = allBranches.get(i);
            System.out.println((i + 1) + ". " + branch.getBranchName() + " (" + branch.getLocation() + ")");
        }
        
        int selection = readBranchSelection(scanner, allBranches.size());
        Branch selectedBranch = allBranches.get(selection - 1);
        
        System.out.println("You are now managing branch: " + selectedBranch.getBranchName());
        
        return new HRManagerBranchService(selectedBranch, employeeRepository, shiftRepository);
    }

    /**
     * Display branch employees
     */
    public void displayBranchEmployees(HRManagerBranchService branchService) {
        List<Employee> employees = branchService.getBranchEmployees();
        
        if (employees.isEmpty()) {
            System.out.println("\nNo employees assigned to this branch.");
            return;
        }
        
        System.out.println("\n=== Employees in " + branchService.getManagedBranch().getBranchName() + " ===");
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            String roles = emp.getAuthorizedRoles().toString();
            System.out.println((i + 1) + ". " + emp.getName() + " - " + roles + " (ID: " + emp.getId() + ")");
        }
    }

    /**
     * Display drivers assigned to the branch
     */
    public void displayBranchDrivers(HRManagerBranchService branchService) {
        List<Employee> drivers = branchService.getBranchDrivers();
        
        if (drivers.isEmpty()) {
            System.out.println("\nNo drivers assigned to this branch.");
            return;
        }
        
        System.out.println("\n=== Drivers in " + branchService.getManagedBranch().getBranchName() + " ===");
        for (int i = 0; i < drivers.size(); i++) {
            Employee driver = drivers.get(i);
            System.out.println((i + 1) + ". " + driver.getName() + " (ID: " + driver.getId() + ")");
        }
    }

    /**
     * Display branch-specific employees (cashiers and storekeepers)
     */
    public void displayBranchSpecificEmployees(HRManagerBranchService branchService) {
        List<Employee> employees = branchService.getBranchSpecificEmployees();
        
        if (employees.isEmpty()) {
            System.out.println("\nNo cashiers or storekeepers assigned to this branch.");
            return;
        }
        
        System.out.println("\n=== Cashiers and Storekeepers in " + branchService.getManagedBranch().getBranchName() + " ===");
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            String roles = emp.getAuthorizedRoles().toString();
            System.out.println((i + 1) + ". " + emp.getName() + " - " + roles + " (ID: " + emp.getId() + ")");
        }
    }

    /**
     * Select an employee from the branch
     */
    public Employee selectBranchEmployee(Scanner scanner, HRManagerBranchService branchService) {
        List<Employee> employees = branchService.getBranchEmployees();
        
        if (employees.isEmpty()) {
            System.out.println("No employees available to select.");
            return null;
        }
        
        System.out.println("\nSelect an employee:");
        for (int i = 0; i < employees.size(); i++) {
            System.out.println((i + 1) + ". " + employees.get(i).getName());
        }
        
        int selection = readEmployeeSelection(scanner, employees.size());
        return employees.get(selection - 1);
    }

    /**
     * Select two employees for shift swap
     */
    public Employee[] selectEmployeesForShiftSwap(Scanner scanner, HRManagerBranchService branchService) {
        System.out.println("\n=== Select First Employee for Shift Swap ===");
        Employee employee1 = selectBranchEmployee(scanner, branchService);
        
        if (employee1 == null) {
            return null;
        }
        
        System.out.println("\n=== Select Second Employee for Shift Swap ===");
        Employee employee2 = selectBranchEmployee(scanner, branchService);
        
        if (employee2 == null) {
            return null;
        }
        
        if (employee1.equals(employee2)) {
            System.out.println("Error: Cannot swap shifts with the same employee.");
            return null;
        }
        
        return new Employee[]{employee1, employee2};
    }

    private int readBranchSelection(Scanner scanner, int maxBranches) {
        while (true) {
            System.out.print("Select branch (1-" + maxBranches + "): ");
            try {
                int selection = Integer.parseInt(scanner.nextLine().trim());
                if (selection >= 1 && selection <= maxBranches) {
                    return selection;
                }
                System.out.println("Invalid selection. Please enter a number between 1 and " + maxBranches);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private int readEmployeeSelection(Scanner scanner, int maxEmployees) {
        while (true) {
            System.out.print("Select employee (1-" + maxEmployees + "): ");
            try {
                int selection = Integer.parseInt(scanner.nextLine().trim());
                if (selection >= 1 && selection <= maxEmployees) {
                    return selection;
                }
                System.out.println("Invalid selection. Please enter a number between 1 and " + maxEmployees);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
