package employee.service;

import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Role;
import employee.domain.Shift;
import dataaccess.repository.EmployeeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dataaccess.repository.ShiftRepository;

/**
 * HRManagerBranchService manages HR operations within a specific branch.
 * 
 * HR Managers can:
 * - Only manage employees assigned to their branch
 * - Assign shifts only to their branch's employees
 * - Swap shifts only between employees of the same branch
 * 
 * Rules:
 * - Drivers (without other roles) are global and cannot be assigned to shifts by branch managers
 * - Employees with Cashier/Storekeeper roles are branch-specific
 * - Drivers with additional roles (Cashier/Storekeeper) are assigned to specific branches
 * - Shift swaps can only happen between employees of the same branch
 */
public class HRManagerBranchService {
    private Branch managedBranch;
    private EmployeeRepository employeeRepository;

    public HRManagerBranchService(Branch managedBranch, EmployeeRepository employeeRepository, ShiftRepository shiftRepository) {
        this.managedBranch = Objects.requireNonNull(managedBranch, "Managed branch cannot be null");
        this.employeeRepository = Objects.requireNonNull(employeeRepository, "Employee repository cannot be null");
    }

    /**
     * Get all employees assigned to this branch
     */
    public List<Employee> getBranchEmployees() {
        List<Employee> result = new ArrayList<>();
        List<Employee> allEmployees = employeeRepository.getAllEmployees();
        
        for (Employee employee : allEmployees) {
            if (employee.getBranch() != null && employee.getBranch().equals(managedBranch)) {
                result.add(employee);
            }
        }
        
        return result;
    }

    /**
     * Get storekeepers and cashiers in this branch
     */
    public List<Employee> getBranchSpecificEmployees() {
        List<Employee> result = new ArrayList<>();
        List<Employee> branchEmployees = getBranchEmployees();
        
        for (Employee employee : branchEmployees) {
            if (!employee.getAuthorizedRoles().contains(Role.DRIVER) &&
                (employee.getAuthorizedRoles().contains(Role.STOREKEEPER) ||
                 employee.getAuthorizedRoles().contains(Role.CASHIER))) {
                result.add(employee);
            }
        }
        
        return result;
    }


    /**
     * Assign a shift to an employee (only if employee belongs to this branch)
     */
    public void assignShiftToEmployee(Employee employee, Shift shift) {
        if (!isBranchEmployee(employee)) {
            throw new IllegalArgumentException("Employee is not assigned to branch: " + managedBranch.getBranchName());
        }
        
        if (isGlobalDriver(employee)) {
            throw new IllegalArgumentException("Global drivers cannot be managed by branch HR managers");
        }
        
        // Create shift assignment
        // This would require ShiftAssignment creation logic
        // For now, just validate
    }

    /**
     * Swap shifts between two employees (both must belong to this branch)
     */
    public void swapShifts(Employee employee1, Shift shift1, Employee employee2, Shift shift2) {
        if (!isBranchEmployee(employee1)) {
            throw new IllegalArgumentException("Employee 1 is not assigned to branch: " + managedBranch.getBranchName());
        }
        
        if (!isBranchEmployee(employee2)) {
            throw new IllegalArgumentException("Employee 2 is not assigned to branch: " + managedBranch.getBranchName());
        }
        
        if (isGlobalDriver(employee1) || isGlobalDriver(employee2)) {
            throw new IllegalArgumentException("Global drivers cannot have shifts managed by branch HR managers");
        }
        
        // Perform shift swap
        // This would require shift swap logic
    }


    /**
     * Get drivers assigned to this branch (drivers with additional roles)
     */
    public List<Employee> getBranchDrivers() {
        List<Employee> result = new ArrayList<>();
        List<Employee> branchEmployees = getBranchEmployees();
        
        for (Employee employee : branchEmployees) {
            if (employee.getAuthorizedRoles().contains(Role.DRIVER)) {
                result.add(employee);
            }
        }
        
        return result;
    }

    /**
     * Check if employee belongs to this branch
     */
    public boolean isBranchEmployee(Employee employee) {
        return employee != null && employee.getBranch() != null && 
               employee.getBranch().equals(managedBranch) &&
               !isGlobalDriver(employee);
    }

    /**
     * Check if employee is a global driver (driver without branch-specific roles)
     */
    private boolean isGlobalDriver(Employee employee) {
        if (employee == null || employee.getBranch() != null) {
            return false;
        }
        
        return employee.getAuthorizedRoles().contains(Role.DRIVER) &&
               !employee.getAuthorizedRoles().contains(Role.CASHIER) &&
               !employee.getAuthorizedRoles().contains(Role.STOREKEEPER);
    }

    /**
     * Get the branch this HR manager manages
     */
    public Branch getManagedBranch() {
        return managedBranch;
    }
}
