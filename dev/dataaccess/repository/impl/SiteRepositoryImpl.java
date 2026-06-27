package dataaccess.repository.impl;
import dataaccess.dao.SiteDAOImpl;
import dataaccess.dto.SiteDto;
import dataaccess.repository.SiteRepository;

public class SiteRepositoryImpl extends AbstractDaoRepository<SiteDto, Integer>
        implements SiteRepository {

    public SiteRepositoryImpl(SiteDAOImpl dao) {
        super(dao);
    }
}