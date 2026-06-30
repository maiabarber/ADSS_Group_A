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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import dataaccess.DatabaseConnection;
import dataaccess.dao.DriverAssignmentRequestDaoImpl;
import dataaccess.dto.DriverAssignmentRequestDto;
import dataaccess.repository.EmployeeRepository;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.ShiftRepository;
import dataaccess.repository.impl.ShiftRepositoryImpl;

public class EmployeeTransportationService {

    private final ShiftRepositoryImpl shiftRepositoryImpl;
    private final EmployeeRepository employeeRepository;
    private final List<DriverAssignmentRequest> driverAssignmentRequests = new ArrayList<>();

    public EmployeeTransportationService(
            ShiftRepository shiftRepository,
            EmployeeRepository employeeRepository) {
        this.shiftRepositoryImpl = (ShiftRepositoryImpl) shiftRepository;
        this.employeeRepository = employeeRepository;
        loadPersistedDriverAssignmentRequests();
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

    private List<ShiftType> getCandidateShiftTypesByTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        List<ShiftType> candidates = new ArrayList<>();

        if (hour >= 6 && hour < 14) {
            candidates.add(ShiftType.MORNING_OVERTIME);
            candidates.add(ShiftType.MORNING);
            return candidates;
        }

        if (hour >= 14 && hour < 16) {
            candidates.add(ShiftType.MORNING_OVERTIME);
            return candidates;
        }

        if (hour >= 16 && hour < 22) {
            candidates.add(ShiftType.EVENING);
            return candidates;
        }

        throw new IllegalArgumentException(
                "No active shift for this time: " + dateTime.toLocalTime());
    }

    public Shift getShiftByDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime is required");
        }

        try {
            for (ShiftType shiftType : getCandidateShiftTypesByTime(dateTime)) {
                Shift shift = shiftRepositoryImpl
                        .findByDateAndType(dateTime.toLocalDate(), shiftType)
                        .orElse(null);
                if (shift != null) {
                    return shift;
                }
            }
            return null;
        } catch (RepositoryException e) {
            return null;
        }
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
        Shift shift = getShiftByDateTime(dateTime);
        ShiftType shiftType = shift == null ? getShiftTypeByTime(dateTime) : shift.getShiftType();
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

        Shift shift = getShiftByDateTime(deliveryDateTime);
        ShiftType shiftType = shift == null ? getShiftTypeByTime(deliveryDateTime) : shift.getShiftType();

        DriverAssignmentRequest request = new DriverAssignmentRequest(
                nextDriverAssignmentRequestId(),
                driverId,
                deliveryId,
                deliveryDateTime,
                shiftType,
                false,
                "Waiting for HR manager to assign driver to shift");

        driverAssignmentRequests.add(request);
        saveDriverAssignmentRequest(request);
    }

    public List<DriverAssignmentRequest> getOpenDriverAssignmentRequests() {
        loadPersistedDriverAssignmentRequests();
        List<DriverAssignmentRequest> openRequests = new ArrayList<>();

        for (DriverAssignmentRequest request : driverAssignmentRequests) {
            if (!request.isHandled()) {
                openRequests.add(request);
            }
        }

        return Collections.unmodifiableList(openRequests);
    }

    public List<DriverAssignmentRequest> getAllDriverAssignmentRequests() {
        loadPersistedDriverAssignmentRequests();
        return Collections.unmodifiableList(driverAssignmentRequests);
    }

    public void markDriverAssignmentRequestHandled(DriverAssignmentRequest request) {
        if (request != null) {
            request.markHandled();
            saveDriverAssignmentRequest(request);
        }
    }

    private void loadPersistedDriverAssignmentRequests() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            List<DriverAssignmentRequestDto> rows =
                    new DriverAssignmentRequestDaoImpl(connection).findAll();
            rows.sort(Comparator.comparingInt(DriverAssignmentRequestDto::getRequestId));

            driverAssignmentRequests.clear();
            for (DriverAssignmentRequestDto row : rows) {
                driverAssignmentRequests.add(toDomain(row));
            }
        } catch (SQLException | RepositoryException e) {
            throw new IllegalStateException("Failed to load driver assignment requests", e);
        }
    }

    private void saveDriverAssignmentRequest(DriverAssignmentRequest request) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            new DriverAssignmentRequestDaoImpl(connection).createOrUpdate(toDto(request));
        } catch (SQLException | RepositoryException e) {
            throw new IllegalStateException("Failed to save driver assignment request", e);
        }
    }

    private int nextDriverAssignmentRequestId() {
        loadPersistedDriverAssignmentRequests();
        int maxId = 0;
        for (DriverAssignmentRequest request : driverAssignmentRequests) {
            if (request.getRequestId() > maxId) {
                maxId = request.getRequestId();
            }
        }
        return maxId + 1;
    }

    private DriverAssignmentRequest toDomain(DriverAssignmentRequestDto dto) {
        return new DriverAssignmentRequest(
                dto.getRequestId(),
                dto.getDriverId(),
                dto.getDeliveryId(),
                LocalDateTime.parse(dto.getDeliveryDateTime()),
                ShiftType.valueOf(dto.getShiftType()),
                dto.isHandled(),
                dto.getStatusMessage());
    }

    private DriverAssignmentRequestDto toDto(DriverAssignmentRequest request) {
        return new DriverAssignmentRequestDto(
                request.getRequestId(),
                request.getDriverId(),
                request.getDeliveryId(),
                request.getDeliveryDateTime().toString(),
                request.getShiftType().name(),
                request.isHandled(),
                request.getStatusMessage());
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

    public boolean canRequestDriverForDeliveryShift(String driverId, LocalDateTime deliveryDateTime) {
        if (driverId == null || driverId.isBlank() || deliveryDateTime == null) {
            return false;
        }

        Shift shift = getShiftByDateTime(deliveryDateTime);
        if (shift == null) {
            return false;
        }

        Employee driver = null;
        try {
            for (Employee employee : employeeRepository.findAll()) {
                if (employee != null && driverId.equals(employee.getId())) {
                    driver = employee;
                    break;
                }
            }
        } catch (RepositoryException e) {
            return false;
        }

        if (driver == null
                || driver.isFired()
                || !driver.getAuthorizedRoles().contains(Role.DRIVER)
                || !canEmployeeWorkAt(driver, deliveryDateTime)) {
            return false;
        }

        for (ShiftAssignment assignment : shift.getAssignments()) {
            if (assignment == null
                    || assignment.getEmployee() == null
                    || !driverId.equals(assignment.getEmployee().getId())) {
                continue;
            }

            return assignment.getRole() == Role.DRIVER
                    && assignment.isApproved()
                    && !assignment.isCancellationRequested();
        }

        return true;
    }

    public boolean assignDriverToDeliveryShift(String driverId, LocalDateTime deliveryDateTime)
            throws RepositoryException {
        if (driverId == null || driverId.isBlank() || deliveryDateTime == null) {
            return false;
        }

        Shift shift = getShiftByDateTime(deliveryDateTime);
        if (shift == null) {
            return false;
        }

        Employee driver = null;
        for (Employee employee : employeeRepository.findAll()) {
            if (employee != null && driverId.equals(employee.getId())) {
                driver = employee;
                break;
            }
        }

        if (driver == null || driver.isFired() || !driver.getAuthorizedRoles().contains(Role.DRIVER)) {
            return false;
        }

        if (!canEmployeeWorkAt(driver, deliveryDateTime)) {
            return false;
        }

        for (ShiftAssignment assignment : shift.getAssignments()) {
            if (assignment != null
                    && assignment.getEmployee() != null
                    && driverId.equals(assignment.getEmployee().getId())) {
                return assignment.getRole() == Role.DRIVER && assignment.isApproved();
            }
        }

        ShiftAssignment assignment = new ShiftAssignment(driver, shift, Role.DRIVER);
        assignment.setApproved(true);
        shift.addAssignment(assignment);
        shiftRepositoryImpl.save(shift);
        return true;
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
