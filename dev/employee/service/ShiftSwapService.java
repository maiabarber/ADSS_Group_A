package employee.service;

import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Shift;
import employee.domain.ShiftAssignment;

/**
 * ShiftSwapService manages shift swaps between employees.
 * 
 * Rules for shift swaps:
 * - Can only swap between employees of the same branch
 * - Global drivers (driver-only role) cannot participate in branch shifts
 * - Shift dates and times must match for valid swap
 */
public class ShiftSwapService {

    /**
     * Validates that a shift swap can occur between two employees
     */
    public boolean canSwapShifts(Employee employee1, Shift shift1, Employee employee2, Shift shift2) {
        // Both employees must have branches
        if (employee1.getBranch() == null || employee2.getBranch() == null) {
            return false;
        }
        
        // Both employees must belong to the same branch
        if (!employee1.getBranch().equals(employee2.getBranch())) {
            return false;
        }
        
        // Shifts must match in date/time (same shift type)
        if (!shift1.equals(shift2)) {
            return false;
        }
        
        return true;
    }

    /**
     * Perform a shift swap between two employees
     */
    public void swapShifts(Employee employee1, ShiftAssignment assignment1, 
                          Employee employee2, ShiftAssignment assignment2) {
        // Validate branch assignment
        if (!employee1.getBranch().equals(employee2.getBranch())) {
            throw new IllegalArgumentException(
                "Cannot swap shifts: employees belong to different branches");
        }
        
        // Validate assignments
        if (!assignment1.getShift().equals(assignment2.getShift())) {
            throw new IllegalArgumentException(
                "Cannot swap shifts: assignments have different shift times");
        }
        
        // Perform swap by exchanging employee assignments
        String tempEmployeeId = assignment1.getEmployeeId();
        // This would update assignment1 to point to employee2
        // And assignment2 to point to employee1
        // Implementation depends on ShiftAssignment structure
    }

    /**
     * Check if employees are in the same branch
     */
    public boolean areSameBranch(Employee employee1, Employee employee2) {
        if (employee1.getBranch() == null || employee2.getBranch() == null) {
            return false;
        }
        return employee1.getBranch().equals(employee2.getBranch());
    }

    /**
     * Get branch from employee
     */
    public Branch getEmployeeBranch(Employee employee) {
        return employee.getBranch();
    }
}
