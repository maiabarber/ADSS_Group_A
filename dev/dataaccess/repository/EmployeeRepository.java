package dataaccess.repository;

import dataaccess.dto.EmployeeDto;
import employee.repository.Repository;

public interface EmployeeRepository extends Repository<EmployeeDto, String> {
}