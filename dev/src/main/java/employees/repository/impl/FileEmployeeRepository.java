package employees.repository.impl;

import employees.domain.Employee;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileEmployeeRepository implements EmployeeRepository {
    private final Path filePath;

    public FileEmployeeRepository(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Employee save(Employee employee) throws RepositoryException {
        if (employee == null) {
            throw new RepositoryException("Employee cannot be null");
        }
        if (employee.getId() == null || employee.getId().isEmpty()) {
            throw new RepositoryException("Employee id cannot be null or blank");
        }

        List<Employee> employees = loadEmployees();
        employees = employees.stream()
            .filter(existing -> !existing.getId().equals(employee.getId()))
            .collect(Collectors.toCollection(ArrayList::new));
        employees.add(employee);
        writeEmployees(employees);
        return employee;
    }

    @Override
    public Optional<Employee> findById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }

        return loadEmployees().stream()
            .filter(employee -> id.equals(employee.getId()))
            .findFirst();
    }

    @Override
    public List<Employee> findAll() throws RepositoryException {
        return new ArrayList<>(loadEmployees());
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return;
        }

        List<Employee> employees = loadEmployees();
        List<Employee> filtered = employees.stream()
            .filter(employee -> !id.equals(employee.getId()))
            .collect(Collectors.toCollection(ArrayList::new));

        if (filtered.size() != employees.size()) {
            writeEmployees(filtered);
        }
    }

    private List<Employee> loadEmployees() throws RepositoryException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {
            Object data = in.readObject();
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Employee> employees = (List<Employee>) data;
                return new ArrayList<>(employees);
            }
            throw new RepositoryException("Employee repository file contains invalid data");
        } catch (IOException | ClassNotFoundException e) {
            throw new RepositoryException("Failed to load employees", e);
        }
    }

    private void writeEmployees(List<Employee> employees) throws RepositoryException {
        try {
            Files.createDirectories(filePath.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath)))) {
                out.writeObject(employees);
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to save employees", e);
        }
    }
}
