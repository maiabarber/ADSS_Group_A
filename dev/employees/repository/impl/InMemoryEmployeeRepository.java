package employees.repository.impl;

import employees.domain.Employee;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * InMemoryEmployeeRepository class provides an in-memory implementation of the EmployeeRepository interface.
 * It uses a HashMap to store Employee objects by their ID, allowing for basic CRUD operations.
 */
public class InMemoryEmployeeRepository implements EmployeeRepository {
    private final Map<String, Employee> employeesById = new HashMap<>();

    @Override
    public Employee save(Employee employee) throws RepositoryException {
        if (employee == null) {
            throw new RepositoryException("Employee cannot be null");
        }
        if (employee.getId() == null || employee.getId().isEmpty()) {
            throw new RepositoryException("Employee id cannot be null or blank");
        }

        employeesById.put(employee.getId(), employee);
        return employee;
    }

    @Override
    public Optional<Employee> findById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(employeesById.get(id));
    }

    @Override
    public List<Employee> findAll() throws RepositoryException {
        return new ArrayList<>(employeesById.values());
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return;
        }
        employeesById.remove(id);
    }
}