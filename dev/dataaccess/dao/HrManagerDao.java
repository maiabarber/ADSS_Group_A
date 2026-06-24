package dataaccess.dao;

import dataaccess.dto.HrManagerDto;
import dataaccess.dto.UserDto;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class HrManagerDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<HrManagerDto> findAll() throws SQLException {
        return employeeDataAccess.listUsers().stream()
                .filter(HrManagerDto.class::isInstance)
                .map(HrManagerDto.class::cast)
                .collect(Collectors.toList());
    }

    public List<UserDto> findAllUsers() throws SQLException {
        return employeeDataAccess.listUsers();
    }
}