package dataaccess.dao;

import dataaccess.dto.EmployeeDto;
import dataaccess.dto.SalaryDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SalaryDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<SalaryDto> findAll() throws SQLException {
        return employeeDataAccess.listEmployees().stream()
                .map(EmployeeDto::getSalary)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}