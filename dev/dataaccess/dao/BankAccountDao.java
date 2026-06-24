package dataaccess.dao;

import dataaccess.dto.BankAccountDto;
import dataaccess.dto.EmployeeDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BankAccountDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<BankAccountDto> findAll() throws SQLException {
        return employeeDataAccess.listEmployees().stream()
                .map(EmployeeDto::getBankAccount)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}