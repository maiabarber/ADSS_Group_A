package dataaccess.dao;

import dataaccess.dto.EmployeeDto;
import dataaccess.dto.PreferenceDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PreferenceDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<PreferenceDto> findAll() throws SQLException {
        return employeeDataAccess.listEmployees().stream()
                .map(EmployeeDto::getWeeklyAvailabilityRequest)
                .filter(Objects::nonNull)
                .flatMap(request -> request.getPreferences().stream())
                .collect(Collectors.toList());
    }
}