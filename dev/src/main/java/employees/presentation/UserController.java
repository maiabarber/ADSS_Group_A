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

    public boolean updateEmployeeDetails(
        User requestedBy,
        Employee employee,
        String newName,
        Double newGlobalSalary,
        Double newHourlySalary,
        Boolean newCanManageShift,
        EmployeeRepository employeeRepository
    ) throws RepositoryException {
        if (!(requestedBy instanceof HR_Manager) || !((HR_Manager) requestedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can update employee details");
        }

        if (newName != null && !newName.isEmpty()) {
            employee.setName(newName);
        }
        if (newGlobalSalary != null) {
            employee.getSalary().setGlobalSalary(newGlobalSalary);
            if (employee.getEmploymentTerms() != null) {
                employee.getEmploymentTerms().setGlobalSalary(newGlobalSalary);
            }
        }
        if (newHourlySalary != null) {
            employee.getSalary().setHourlySalary(newHourlySalary);
            if (employee.getEmploymentTerms() != null) {
                employee.getEmploymentTerms().setHourlySalary(newHourlySalary);
            }
        }
        if (newCanManageShift != null) {
            employee.setCanManageShift(newCanManageShift);
        }

        employeeRepository.save(employee);

        for (Employee existingEmployee : employees) {
            if (employee.getId().equals(existingEmployee.getId())) {
                if (newName != null && !newName.isEmpty()) {
                    existingEmployee.setName(newName);
                }
                if (newGlobalSalary != null) {
                    existingEmployee.getSalary().setGlobalSalary(newGlobalSalary);
                    if (existingEmployee.getEmploymentTerms() != null) {
                        existingEmployee.getEmploymentTerms().setGlobalSalary(newGlobalSalary);
                    }
                }
                if (newHourlySalary != null) {
                    existingEmployee.getSalary().setHourlySalary(newHourlySalary);
                    if (existingEmployee.getEmploymentTerms() != null) {
                        existingEmployee.getEmploymentTerms().setHourlySalary(newHourlySalary);
                    }
                }
                if (newCanManageShift != null) {
                    existingEmployee.setCanManageShift(newCanManageShift);
                }
            }
        }

        return true;
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