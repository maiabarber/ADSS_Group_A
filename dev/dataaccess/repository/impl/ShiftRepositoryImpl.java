package dataaccess.repository.impl;
import dataaccess.dao.ShiftDaoImpl;
import dataaccess.dto.ShiftDto;
import dataaccess.repository.ShiftRepository;

public class ShiftRepositoryImpl extends AbstractDaoRepository<ShiftDto, Integer>
        implements ShiftRepository {

    public ShiftRepositoryImpl(ShiftDaoImpl dao) {
        super(dao);
    }
}