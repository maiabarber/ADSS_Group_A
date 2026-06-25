package employee.service;

import employee.domain.Constraint;
import employee.domain.DriverAssignmentRequest;
import employee.domain.Employee;
import employee.domain.Preference;
import employee.domain.Role;
import employee.domain.Shift;
import employee.domain.ShiftAssignment;
import employee.domain.ShiftType;
import employee.domain.WeeklyAvailabilityRequest;
import employee.repository.EmployeeRepository;
import employee.repository.RepositoryException;
import employee.repository.ShiftRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmployeeTransportationService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final List<DriverAssignmentRequest> driverAssignmentRequests = new ArrayList<>();

    public EmployeeTransportationService(
            ShiftRepository shiftRepository,
            EmployeeRepository employeeRepository) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
    }

    private ShiftType getShiftTypeByTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();

        if (hour >= 6 && hour < 14) {
            return ShiftType.MORNING;
        }

        if (hour >= 14 && hour < 16) {
            return ShiftType.MORNING_OVERTIME;
        }

        if (hour >= 16 && hour < 22) {
            return ShiftType.EVENING;
        }

        throw new IllegalArgumentException(
                "No active shift for this time: " + dateTime.toLocalTime());
    }

    public Shift getShiftByDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime is required");
        }

        ShiftType shiftType = getShiftTypeByTime(dateTime);

        return shiftRepository
                .findByDateAndType(dateTime.toLocalDate(), shiftType)
                .orElse(null);
    }

    public List<Employee> getAvailableDriversForDelivery(LocalDateTime deliveryDateTime) {
        if (deliveryDateTime == null) {
            return Collections.emptyList();
        }

        List<Employee> availableDrivers = new ArrayList<>();

        try {
            for (Employee employee : employeeRepository.findAll()) {
                if (employee == null || employee.isFired()) {
                    continue;
                }

                if (!employee.getAuthorizedRoles().contains(Role.DRIVER)) {
                    continue;
                }

                if (canEmployeeWorkAt(employee, deliveryDateTime)) {
                    availableDrivers.add(employee);
                }
            }
        } catch (RepositoryException e) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(availableDrivers);
    }

    private boolean canEmployeeWorkAt(Employee employee, LocalDateTime dateTime) {
        ShiftType shiftType = getShiftTypeByTime(dateTime);
        WeeklyAvailabilityRequest availability = employee.getWeeklyAvailabilityRequest();

        if (employee.getFixedDayOff() != null
                && employee.getFixedDayOff() == dateTime.getDayOfWeek()) {
            return false;
        }

        if (availability == null) {
            return true;
        }

        for (Constraint constraint : availability.getConstraints()) {
            if (constraint.getDay() == dateTime.getDayOfWeek()
                    && constraint.getShiftType() == shiftType) {
                return false;
            }
        }

        for (Preference preference : availability.getPreferences()) {
            if (preference.getDay() == dateTime.getDayOfWeek()
                    && preference.getShiftType() == shiftType) {
                return true;
            }
        }

        return true;
    }

    public void createDriverAssignmentRequest(
            String driverId,
            int deliveryId,
            LocalDateTime deliveryDateTime) {

        if (driverId == null || driverId.isBlank()) {
            throw new IllegalArgumentException("driverId is required");
        }

        if (deliveryDateTime == null) {
            throw new IllegalArgumentException("deliveryDateTime is required");
        }

        ShiftType shiftType = getShiftTypeByTime(deliveryDateTime);

        DriverAssignmentRequest request = new DriverAssignmentRequest(
                driverId,
                deliveryId,
                deliveryDateTime,
                shiftType);

        driverAssignmentRequests.add(request);
    }

    public List<DriverAssignmentRequest> getOpenDriverAssignmentRequests() {
        List<DriverAssignmentRequest> openRequests = new ArrayList<>();

        for (DriverAssignmentRequest request : driverAssignmentRequests) {
            if (!request.isHandled()) {
                openRequests.add(request);
            }
        }

        return Collections.unmodifiableList(openRequests);
    }

    public List<DriverAssignmentRequest> getAllDriverAssignmentRequests() {
        return Collections.unmodifiableList(driverAssignmentRequests);
    }

    public void markDriverAssignmentRequestHandled(DriverAssignmentRequest request) {
        if (request != null) {
            request.markHandled();
        }
    }

    public boolean isDriverAssignedToShift(String driverId, LocalDateTime deliveryDateTime) {
        if (driverId == null || driverId.isBlank() || deliveryDateTime == null) {
            return false;
        }

        Shift shift = getShiftByDateTime(deliveryDateTime);
        if (shift == null) {
            return false;
        }

        for (ShiftAssignment assignment : shift.getAssignments()) {
            if (assignment == null || assignment.getEmployee() == null) {
                continue;
            }

            Employee employee = assignment.getEmployee();

            if (driverId.equals(employee.getId())
                    && assignment.getRole() == Role.DRIVER
                    && assignment.isApproved()
                    && !assignment.isCancellationRequested()
                    && !employee.isFired()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasStorekeeperInShift(LocalDateTime arrivalDateTime) {
        if (arrivalDateTime == null) {
            return false;
        }

        Shift shift = getShiftByDateTime(arrivalDateTime);
        if (shift == null) {
            return false;
        }

        for (ShiftAssignment assignment : shift.getAssignments()) {
            if (assignment == null || assignment.getEmployee() == null) {
                continue;
            }

            Employee employee = assignment.getEmployee();

            if (assignment.getRole() == Role.STOREKEEPER
                    && assignment.isApproved()
                    && !assignment.isCancellationRequested()
                    && !employee.isFired()) {
                return true;
            }
        }
        return false;
    }
}