package dataaccess.dao;

import dataaccess.dto.EmployeeDto;
import dataaccess.dto.EmploymentTermsDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EmploymentTermsDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<EmploymentTermsDto> findAll() throws SQLException {
        return employeeDataAccess.listEmployees().stream()
                .map(EmployeeDto::getEmploymentTerms)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}