package dataaccess.dao;

import dataaccess.dto.EmployeeDto;
import dataaccess.dto.WeeklyAvailabilityRequestDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WeeklyAvailabilityRequestDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<WeeklyAvailabilityRequestDto> findAll() throws SQLException {
        return employeeDataAccess.listEmployees().stream()
                .map(EmployeeDto::getWeeklyAvailabilityRequest)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}