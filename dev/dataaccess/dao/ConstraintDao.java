package dataaccess.dao;

import dataaccess.dto.ConstraintDto;
import dataaccess.dto.EmployeeDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConstraintDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<ConstraintDto> findAll() throws SQLException {
        return employeeDataAccess.listEmployees().stream()
                .map(EmployeeDto::getWeeklyAvailabilityRequest)
                .filter(Objects::nonNull)
                .flatMap(request -> request.getConstraints().stream())
                .collect(Collectors.toList());
    }
}