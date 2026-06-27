package dataaccess.repository.impl;
import dataaccess.dao.BranchDAOImpl;
import dataaccess.dto.BranchDto;
import dataaccess.repository.BranchRepository;

public class BranchRepositoryImpl extends AbstractDaoRepository<BranchDto, Integer>
        implements BranchRepository {

    public BranchRepositoryImpl(BranchDAOImpl dao) {
        super(dao);
    }
}