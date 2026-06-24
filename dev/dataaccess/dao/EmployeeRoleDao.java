package dataaccess.dao;

import dataaccess.dto.EmployeeRoleDto;

import java.sql.SQLException;
import java.util.List;

public class EmployeeRoleDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<EmployeeRoleDto> findAll() throws SQLException {
        return employeeDataAccess.listEmployeeRoles();
    }
}