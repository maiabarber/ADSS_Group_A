package employee.service;

import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Role;
import dataaccess.repository.EmployeeRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * BranchEmployeeService manages the relationship between branches and employees.
 * 
 * Rules:
 * - Drivers are global employees (branch = null) and can work in any branch
 * - Cashiers and Storekeepers are branch-specific (branch != null)
 * - Each branch is assigned a specific set of storekeepers and cashiers
 * - Drivers can be assigned to work with any branch's storekeepers/cashiers
 */
public class BranchEmployeeService {
    private EmployeeRepository employeeRepository;

    public BranchEmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all employees assigned to a specific branch (storekeepers and cashiers only)
     */
    public List<Employee> getEmployeesForBranch(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("Branch cannot be null");
        }
        
        List<Employee> result = new ArrayList<>();
        List<Employee> allEmployees = employeeRepository.getAllEmployees();
        
        for (Employee employee : allEmployees) {
            if (employee.getBranch() != null && employee.getBranch().equals(branch)) {
                result.add(employee);
            }
        }
        
        return result;
    }

    /**
     * Get all drivers (global employees)
     */
    public List<Employee> getAllDrivers() {
        List<Employee> result = new ArrayList<>();
        List<Employee> allEmployees = employeeRepository.getAllEmployees();
        
        for (Employee employee : allEmployees) {
            // Global drivers: have DRIVER role but no CASHIER/STOREKEEPER, and no branch
            if (employee.getAuthorizedRoles().contains(Role.DRIVER) && 
                !employee.getAuthorizedRoles().contains(Role.CASHIER) &&
                !employee.getAuthorizedRoles().contains(Role.STOREKEEPER) &&
                employee.getBranch() == null) {
                result.add(employee);
            }
        }
        
        return result;
    }

    /**
     * Get all branch-specific employees (storekeepers and cashiers)
     */
    public List<Employee> getAllBranchSpecificEmployees() {
        List<Employee> result = new ArrayList<>();
        List<Employee> allEmployees = employeeRepository.getAllEmployees();
        
        for (Employee employee : allEmployees) {
            if ((employee.getAuthorizedRoles().contains(Role.CASHIER) ||
                 employee.getAuthorizedRoles().contains(Role.STOREKEEPER)) &&
                employee.getBranch() != null) {
                result.add(employee);
            }
        }
        
        return result;
    }

    /**
     * Get all drivers with additional roles assigned to branches (mixed role drivers)
     */
    public List<Employee> getAllMixedRoleDrivers() {
        List<Employee> result = new ArrayList<>();
        List<Employee> allEmployees = employeeRepository.getAllEmployees();
        
        for (Employee employee : allEmployees) {
            // Mixed role drivers: have DRIVER + (CASHIER or STOREKEEPER) + branch assignment
            if (employee.getAuthorizedRoles().contains(Role.DRIVER) &&
                (employee.getAuthorizedRoles().contains(Role.CASHIER) ||
                 employee.getAuthorizedRoles().contains(Role.STOREKEEPER)) &&
                employee.getBranch() != null) {
                result.add(employee);
            }
        }
        
        return result;
    }

    /**
     * Validate that an employee has the correct branch assignment based on their role
     */
    public boolean isEmployeeBranchAssignmentValid(Employee employee) {
        if (employee == null) {
            return false;
        }
        
        boolean isDriver = employee.getAuthorizedRoles().contains(Role.DRIVER);
        boolean isBranchSpecific = employee.getAuthorizedRoles().contains(Role.CASHIER) ||
                                  employee.getAuthorizedRoles().contains(Role.STOREKEEPER);
        
        if (isDriver && employee.getBranch() != null) {
            return false; // Driver should not have a branch
        }
        
        if (isBranchSpecific && employee.getBranch() == null) {
            return false; // Branch-specific employee must have a branch
        }
        
        return true;
    }
}
