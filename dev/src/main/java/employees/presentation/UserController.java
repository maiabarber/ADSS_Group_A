package employees.presentation;

import employees.domain.Employee;
import employees.domain.HR_Manager;
import employees.domain.User;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.service.AuthenticationService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * UserController class manages user-related operations such as adding employees, updating employee details,
 * firing employees, and approving shift managers. It interacts with the authentication service and employee repository
 * to perform these operations while ensuring that only HR managers have the necessary permissions.
 */
public class UserController {
    public static final int DEFAULT_VACATION_DAYS = 10;
    private final List<Employee> employees = new ArrayList<>();
    private final AuthenticationService authenticationService;
    private final EmployeeRepository employeeRepository;
    private HR_Manager manager;

    public UserController(AuthenticationService authenticationService, EmployeeRepository employeeRepository) {
        this.authenticationService = authenticationService;
        this.employeeRepository = employeeRepository;
    }

    public void setManager(HR_Manager manager) {
        this.manager = manager;
    }

    public HR_Manager getManager() {
        return manager;
    }

    public List<Employee> getEmployees() {
        try {
            return Collections.unmodifiableList(employeeRepository.findAll());
        } catch (RepositoryException e) {
            return Collections.emptyList();
        }
    }

    public void addEmployee(User requestedBy, Employee employee)
        throws RepositoryException {
        if (!(requestedBy instanceof HR_Manager) || !((HR_Manager) requestedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can add employees");
        }

        if (employee.getEmploymentTerms() != null) {
            employee.getEmploymentTerms().setVacationDays(DEFAULT_VACATION_DAYS);
        }

        authenticationService.registerUser(employee);
        employeeRepository.save(employee);
    }

    public boolean updateEmployeeDetails(
        User requestedBy,
        Employee employee,
        String newName,
        Double newGlobalSalary,
        Double newHourlySalary,
        Boolean newCanManageShift
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
        return true;
    }

    public boolean fireEmployee(User requestedBy, String employeeId)
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
        return true;
    }

    public void approveAsShiftManager(User requestedBy, String employeeId) throws RepositoryException {
        if (!(requestedBy instanceof HR_Manager) || !((HR_Manager) requestedBy).isHRManager()) {
            throw new IllegalArgumentException("Only HR manager can approve shift manager");
        }

        Optional<Employee> employeeOptional = employeeRepository.findById(employeeId);
        if (!employeeOptional.isPresent()) {
            throw new IllegalArgumentException("Employee not found");
        }

        Employee employee = employeeOptional.get();
        employee.approveAsShiftManager(requestedBy);
        employeeRepository.save(employee);
    }
}