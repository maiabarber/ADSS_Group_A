package dataaccess.repository;

import employee.domain.Employee;
import java.util.Collections;
import java.util.List;

public interface EmployeeRepository extends Repository<Employee, String> {
    default List<Employee> getAllEmployees() {
        try {
            return findAll();
        } catch (RepositoryException e) {
            return Collections.emptyList();
        }
    }
}
