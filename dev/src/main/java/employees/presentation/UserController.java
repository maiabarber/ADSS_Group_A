package employees.presentation;

import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.service.AuthenticationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public void addEmployee(Employee employee, AuthenticationService authenticationService, EmployeeRepository employeeRepository)
        throws RepositoryException {
        authenticationService.registerUser(employee);
        employeeRepository.save(employee);
        employees.add(employee);
    }
}
