package employee.repository;

import employee.domain.Employee;
import java.util.List;
import java.util.Collections;

public interface EmployeeRepository extends Repository<Employee, String> {
    default List<Employee> getAllEmployees() {
        try {
            return findAll();
        } catch (RepositoryException e) {
            return Collections.emptyList();
        }
    }
}