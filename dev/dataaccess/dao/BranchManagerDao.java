package dataaccess.dao;

import dataaccess.dto.BranchManagerDto;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class BranchManagerDao {
    private final EmployeeDataAccess employeeDataAccess = new EmployeeDataAccess();

    public List<BranchManagerDto> findAll() throws SQLException {
        return Collections.singletonList(employeeDataAccess.getBranchManagerView());
    }
}