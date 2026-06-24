package employee.service;

import employee.domain.Employee;
import employee.domain.Role;
import employee.domain.Shift;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DriverShiftAssignmentService manages driver shift assignments.
 * 
 * Rules:
 * - A driver can be assigned to any shift
 * - When a driver works in a shift, they work alongside storekeepers/cashiers from that branch
 * - A driver can work multiple shifts at different branches
 * - Drivers should be assigned to the same shift as the storekeeper they're supporting
 */
public class DriverShiftAssignmentService {
    private BranchEmployeeService branchEmployeeService;

    public DriverShiftAssignmentService(BranchEmployeeService branchEmployeeService) {
        this.branchEmployeeService = branchEmployeeService;
    }

    /**
     * Get all drivers who can support a specific shift with a storekeeper
     */
    public List<Employee> getAvailableDriversForShift(Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("Shift cannot be null");
        }
        
        List<Employee> availableDrivers = new ArrayList<>();
        List<Employee> allDrivers = branchEmployeeService.getAllDrivers();
        
        for (Employee driver : allDrivers) {
            if (!driver.isFired() && canDriverWorkShift(driver, shift)) {
                availableDrivers.add(driver);
            }
        }
        
        return availableDrivers;
    }

    /**
     * Check if a driver can be assigned to a specific shift
     * (they are not already assigned to conflicting shifts)
     */
    public boolean canDriverWorkShift(Employee driver, Shift shift) {
        if (!isDriver(driver)) {
            throw new IllegalArgumentException("Employee is not a driver");
        }
        
        // Check if driver has conflicting shifts (same date/time)
        // This would require access to shift assignments which we may not have
        // For now, we just validate the driver is available
        return true;
    }

    /**
     * Validate that a driver is being assigned to work with a storekeeper
     */
    public boolean isDriverAssignedWithStoreKeeper(Employee driver, Employee storeKeeper, Shift shift) {
        if (!isDriver(driver)) {
            throw new IllegalArgumentException("Employee is not a driver");
        }
        
        if (!isStoreKeeperOrCashier(storeKeeper)) {
            throw new IllegalArgumentException("Employee is not a storekeeper or cashier");
        }
        
        // Driver can work with any storekeeper from any branch
        // since drivers are global employees
        return true;
    }

    /**
     * Get drivers assigned to a specific branch's shifts (for a date range)
     */
    public List<Employee> getDriversWorkingInBranchOnDate(String branchId, LocalDate date) {
        // This would require access to shift assignments
        // For now, we just return an empty list
        return new ArrayList<>();
    }

    private boolean isDriver(Employee employee) {
        return employee != null && employee.getAuthorizedRoles().contains(Role.DRIVER) && employee.getBranch() == null;
    }

    private boolean isStoreKeeperOrCashier(Employee employee) {
        if (employee == null) {
            return false;
        }
        return (employee.getAuthorizedRoles().contains(Role.STOREKEEPER) ||
                employee.getAuthorizedRoles().contains(Role.CASHIER)) &&
                employee.getBranch() != null;
    }
}
