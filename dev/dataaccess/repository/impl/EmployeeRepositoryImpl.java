package dataaccess.repository.impl;
import dataaccess.dao.EmployeeDAOImpl;
import dataaccess.dto.EmployeeDto;
import dataaccess.repository.EmployeeRepository;

public class EmployeeRepositoryImpl extends AbstractDaoRepository<EmployeeDto, String>
        implements EmployeeRepository {

    public EmployeeRepositoryImpl(EmployeeDAOImpl dao) {
        super(dao);
    }
}
