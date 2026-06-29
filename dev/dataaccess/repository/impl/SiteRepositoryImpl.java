// package dataaccess.repository.impl;
// import dataaccess.dao.SiteDAOImpl;
// import dataaccess.dto.SiteDto;
// import dataaccess.repository.SiteRepository;

// public class SiteRepositoryImpl extends AbstractDaoRepository<SiteDto, Integer>
//         implements SiteRepository {

//     public SiteRepositoryImpl(SiteDAOImpl dao) {
//         super(dao);
//     }
// }



package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.SiteDAOImpl;
import dataaccess.dto.SiteDto;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.SiteRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SiteRepositoryImpl implements SiteRepository {
    private final SiteDAOImpl siteDao;

    public SiteRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize SiteRepository", e);
        }

        this.siteDao = null;
    }

    public SiteRepositoryImpl(SiteDAOImpl siteDao) {
        this.siteDao = siteDao;
    }

    @Override
    public SiteDto save(SiteDto dto) throws RepositoryException {
        if (dto == null) {
            return null;
        }

        if (siteDao != null) {
            siteDao.update(dto);
            return dto;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            SiteDAOImpl dao = new SiteDAOImpl(connection);
            dao.update(dto);
            return dto;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save site", e);
        }
    }

    @Override
    public Optional<SiteDto> findById(Integer id) throws RepositoryException {
        if (id == null) {
            return Optional.empty();
        }

        if (siteDao != null) {
            return Optional.ofNullable(siteDao.findbyId(String.valueOf(id)));
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            SiteDAOImpl dao = new SiteDAOImpl(connection);
            return Optional.ofNullable(dao.findbyId(String.valueOf(id)));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find site", e);
        }
    }

    @Override
    public List<SiteDto> findAll() throws RepositoryException {
        if (siteDao != null) {
            return siteDao.findAll();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            SiteDAOImpl dao = new SiteDAOImpl(connection);
            return dao.findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load sites", e);
        }
    }

    @Override
    public void deleteById(Integer id) throws RepositoryException {
        if (id == null) {
            return;
        }

        if (siteDao != null) {
            siteDao.delete(String.valueOf(id));
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            SiteDAOImpl dao = new SiteDAOImpl(connection);
            dao.delete(String.valueOf(id));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete site", e);
        }
    }
}