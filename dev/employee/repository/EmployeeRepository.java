package employee.repository;

import employee.domain.Employee;
import java.util.List;

public interface EmployeeRepository extends Repository<Employee, String> {
	default List<Employee> getAllEmployees() throws RepositoryException {
		return findAll();
	}
}
