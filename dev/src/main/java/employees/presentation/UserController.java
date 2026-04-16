package employees.presentation;

import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.domain.User;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.service.AuthenticationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserController {
    private final List<Employee> employees = new ArrayList<>();
    private HR_Manager manager;

    public void setManager(HR_Manager manager) {
        this.manager = manager;
    }

    public HR_Manager getManager() {
        return manager;
    }

    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }

    public void addEmployee(User requestedBy, Employee employee, AuthenticationService authenticationService, EmployeeRepository employeeRepository)
        throws RepositoryException {
        if (!(requestedBy instanceof HR_Manager) || !((HR_Manager) requestedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can add employees");
        }

        authenticationService.registerUser(employee);
        employeeRepository.save(employee);
        employees.add(employee);
    }

    public boolean fireEmployee(User requestedBy, String employeeId, AuthenticationService authenticationService, EmployeeRepository employeeRepository)
        throws RepositoryException {
        if (!(requestedBy instanceof HR_Manager) || !((HR_Manager) requestedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can fire employees");
        }

        Optional<Employee> employeeOptional = employeeRepository.findById(employeeId);
        if (!employeeOptional.isPresent()) {
            return false;
        }

        Employee employee = employeeOptional.get();
        employee.setFired(true);
        employeeRepository.save(employee);
        authenticationService.registerUser(employee);

        for (Employee existing : employees) {
            if (employeeId.equals(existing.getId())) {
                existing.setFired(true);
            }
        }

        return true;
    }
}
