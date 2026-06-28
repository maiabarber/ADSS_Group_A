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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EmployeeRepositoryImpl implements EmployeeRepository {
    private final EmployeeDAOImpl employeeDao;
    private final Map<String, Employee> savedEmployees = new LinkedHashMap<>();

    public EmployeeRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize EmployeeRepository", e);
        }
        this.employeeDao = null;
    }

    public EmployeeRepositoryImpl(EmployeeDAOImpl employeeDao) {
        this.employeeDao = employeeDao;
    }

    @Override
    public Employee save(Employee employee) throws RepositoryException {
        if (employee == null) {
            return null;
        }

        savedEmployees.put(employee.getId(), employee);

        if (employeeDao != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                saveWithDaos(
                        employee,
                        employeeDao,
                        new UserDAOImpl(connection),
                        new EmployeeRoleDaoImpl(connection));
            } catch (SQLException e) {
                throw new RepositoryException("Failed to save employee", e);
            }
            return employee;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            saveWithDaos(
                    employee,
                    new EmployeeDAOImpl(connection),
                    new UserDAOImpl(connection),
                    new EmployeeRoleDaoImpl(connection));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save employee", e);
        }

        return employee;
    }

    @Override
    public Optional<Employee> findById(String id) throws RepositoryException {
        Employee savedEmployee = savedEmployees.get(id);
        if (savedEmployee != null) {
            return Optional.of(savedEmployee);
        }

        if (employeeDao != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                return findByIdWithDaos(
                        id,
                        employeeDao,
                        new UserDAOImpl(connection),
                        new EmployeeRoleDaoImpl(connection),
                        new BranchDAOImpl(connection));
            } catch (SQLException e) {
                throw new RepositoryException("Failed to find employee", e);
            }
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            return findByIdWithDaos(
                    id,
                    new EmployeeDAOImpl(connection),
                    new UserDAOImpl(connection),
                    new EmployeeRoleDaoImpl(connection),
                    new BranchDAOImpl(connection));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find employee", e);
        }
    }

    @Override
    public List<Employee> findAll() throws RepositoryException {
        Map<String, Employee> employees = new LinkedHashMap<>();
        employees.putAll(savedEmployees);

        if (employeeDao != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                for (Employee employee : findAllWithDaos(
                        employeeDao,
                        new UserDAOImpl(connection),
                        new EmployeeRoleDaoImpl(connection),
                        new BranchDAOImpl(connection))) {
                    employees.putIfAbsent(employee.getId(), employee);
                }
            } catch (SQLException e) {
                throw new RepositoryException("Failed to load employees", e);
            }
        } else {
            try (Connection connection = DatabaseConnection.getConnection()) {
                for (Employee employee : findAllWithDaos(
                        new EmployeeDAOImpl(connection),
                        new UserDAOImpl(connection),
                        new EmployeeRoleDaoImpl(connection),
                        new BranchDAOImpl(connection))) {
                    employees.putIfAbsent(employee.getId(), employee);
                }
            } catch (SQLException e) {
                throw new RepositoryException("Failed to load employees", e);
            }
        }

        return new ArrayList<>(employees.values());
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        savedEmployees.remove(id);

        if (employeeDao != null) {
            employeeDao.delete(id);
            try (Connection connection = DatabaseConnection.getConnection()) {
                new UserDAOImpl(connection).delete(id);
            } catch (SQLException e) {
                throw new RepositoryException("Failed to delete employee user", e);
            }
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            new EmployeeDAOImpl(connection).delete(id);
            new UserDAOImpl(connection).delete(id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete employee", e);
        }
    }

    public List<Employee> getAllEmployees() {
        try {
            return findAll();
        } catch (RepositoryException e) {
            return java.util.Collections.emptyList();
        }
    }

    private void saveWithDaos(
            Employee employee,
            EmployeeDAOImpl employeeDao,
            UserDAOImpl userDao,
            EmployeeRoleDaoImpl roleDao) throws RepositoryException {

        UserDto existingUser = userDao.findbyId(employee.getId());
        boolean isHrManager = existingUser != null && existingUser.isHrManager();

        userDao.createOrUpdate(new UserDto(
                employee.getId(),
                employee.getPassword(),
                isHrManager));

        employeeDao.createOrUpdate(EmployeeMapper.toDto(employee));

        for (Role role : employee.getAuthorizedRoles()) {
            roleDao.createOrUpdate(new EmployeeRoleDto(employee.getId(), role.name()));
        }
    }

    private Optional<Employee> findByIdWithDaos(
            String id,
            EmployeeDAOImpl employeeDao,
            UserDAOImpl userDao,
            EmployeeRoleDaoImpl roleDao,
            BranchDAOImpl branchDao) throws RepositoryException {

        EmployeeDto employeeDto = employeeDao.findbyId(id);
        if (employeeDto == null) {
            return Optional.empty();
        }

        UserDto userDto = userDao.findbyId(id);
        return Optional.of(EmployeeMapper.toDomain(
                employeeDto,
                userDto == null ? "1234" : userDto.getPassword(),
                loadRoles(id, roleDao),
                loadBranch(employeeDto.getBranchId(), branchDao)));
    }

    private List<Employee> findAllWithDaos(
            EmployeeDAOImpl employeeDao,
            UserDAOImpl userDao,
            EmployeeRoleDaoImpl roleDao,
            BranchDAOImpl branchDao) throws RepositoryException {

        List<Employee> employees = new ArrayList<>();
        for (EmployeeDto dto : employeeDao.findAll()) {
            if (savedEmployees.containsKey(dto.getEmployeeId())) {
                continue;
            }

            UserDto userDto = userDao.findbyId(dto.getEmployeeId());
            employees.add(EmployeeMapper.toDomain(
                    dto,
                    userDto == null ? "1234" : userDto.getPassword(),
                    loadRoles(dto.getEmployeeId(), roleDao),
                    loadBranch(dto.getBranchId(), branchDao)));
        }
        return employees;
    }

    private Set<Role> loadRoles(String employeeId, EmployeeRoleDaoImpl roleDao) throws RepositoryException {
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

    private Branch loadBranch(Integer branchId, BranchDAOImpl branchDao) throws RepositoryException {
        if (branchId == null) {
            return null;
        }

        BranchDto branchDto = branchDao.findbyId(String.valueOf(branchId));
        return BranchMapper.toDomain(branchDto);
    }
}
