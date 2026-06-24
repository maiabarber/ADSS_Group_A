package dataaccess.dao;

import dataaccess.dto.DriverAssignmentRequestDto;

import java.sql.SQLException;
import java.util.List;

public class DriverAssignmentRequestDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<DriverAssignmentRequestDto> findAll() throws SQLException {
        return employeeDataAccess.listDriverAssignmentRequests();
    }
}