package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.BranchDAOImpl;
import dataaccess.dao.EmployeeDAOImpl;
import dataaccess.dao.EmployeeRoleDaoImpl;
import dataaccess.dao.UserDAOImpl;
import dataaccess.dto.BranchDto;
import dataaccess.dto.EmployeeDto;
import dataaccess.dto.EmployeeRoleDto;
import dataaccess.dto.UserDto;
import dataaccess.mapper.BranchMapper;
import dataaccess.mapper.EmployeeMapper;
import dataaccess.repository.EmployeeRepository;
import dataaccess.repository.RepositoryException;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EmployeeRepositoryImpl implements EmployeeRepository {
    private final EmployeeDAOImpl employeeDao;
    private final UserDAOImpl userDao;
    private final EmployeeRoleDaoImpl roleDao;
    private final BranchDAOImpl branchDao;

    public EmployeeRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
            Connection connection = DatabaseConnection.getConnection();
            this.employeeDao = new EmployeeDAOImpl(connection);
            this.userDao = new UserDAOImpl(connection);
            this.roleDao = new EmployeeRoleDaoImpl(connection);
            this.branchDao = new BranchDAOImpl(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize EmployeeRepository", e);
        }
    }

    public EmployeeRepositoryImpl(EmployeeDAOImpl employeeDao) {
        this.employeeDao = employeeDao;
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.userDao = new UserDAOImpl(connection);
            this.roleDao = new EmployeeRoleDaoImpl(connection);
            this.branchDao = new BranchDAOImpl(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize EmployeeRepository dependencies", e);
        }
    }

    @Override
    public Employee save(Employee employee) throws RepositoryException {
        UserDto existingUser = userDao.findbyId(employee.getId());

        boolean isHrManager = existingUser != null && existingUser.isHrManager();

        userDao.createOrUpdate(new UserDto(
                employee.getId(),
                employee.getPassword(),
                isHrManager
        ));
        employeeDao.createOrUpdate(EmployeeMapper.toDto(employee));

        for (Role role : employee.getAuthorizedRoles()) {
            roleDao.createOrUpdate(new EmployeeRoleDto(employee.getId(), role.name()));
        }

        return employee;
    }

    @Override
    public Optional<Employee> findById(String id) throws RepositoryException {
        EmployeeDto employeeDto = employeeDao.findbyId(id);
        if (employeeDto == null) {
            return Optional.empty();
        }

        UserDto userDto = userDao.findbyId(id);
        String password = userDto == null ? "1234" : userDto.getPassword();

        return Optional.of(EmployeeMapper.toDomain(
                employeeDto,
                password,
                loadRoles(id),
                loadBranch(employeeDto.getBranchId())
        ));
    }

    @Override
    public List<Employee> findAll() throws RepositoryException {
        List<Employee> employees = new ArrayList<>();
        for (EmployeeDto dto : employeeDao.findAll()) {
            UserDto userDto = userDao.findbyId(dto.getEmployeeId());
            employees.add(EmployeeMapper.toDomain(
                    dto,
                    userDto == null ? "1234" : userDto.getPassword(),
                    loadRoles(dto.getEmployeeId()),
                    loadBranch(dto.getBranchId())
            ));
        }
        return employees;
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        employeeDao.delete(id);
        userDao.delete(id);
    }

    public List<Employee> getAllEmployees() {
        try {
            return findAll();
        } catch (RepositoryException e) {
            return java.util.Collections.emptyList();
        }
    }

    private Set<Role> loadRoles(String employeeId) throws RepositoryException {
        Set<Role> roles = new HashSet<>();
        for (EmployeeRoleDto roleDto : roleDao.findAll()) {
            if (employeeId.equals(roleDto.getEmployeeId())) {
                String roleName = roleDto.getRoleName();
                if (!roleName.equals("HR_MANAGER")) {
                    roles.add(Role.valueOf(roleName));
                }
            }
        }
        return roles;
    }

    private Branch loadBranch(Integer branchId) throws RepositoryException {
        if (branchId == null) {
            return null;
        }
        BranchDto branchDto = branchDao.findbyId(String.valueOf(branchId));
        return BranchMapper.toDomain(branchDto);
    }
}
