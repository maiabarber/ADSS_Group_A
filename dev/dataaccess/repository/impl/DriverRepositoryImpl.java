package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.DriverDAOImpl;
import dataaccess.dto.DriverDto;
import dataaccess.repository.DriverRepository;
import dataaccess.repository.RepositoryException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DriverRepositoryImpl implements DriverRepository {
    private final DriverDAOImpl driverDao;

    public DriverRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize DriverRepository", e);
        }

        this.driverDao = null;
    }

    public DriverRepositoryImpl(DriverDAOImpl driverDao) {
        this.driverDao = driverDao;
    }

    @Override
    public DriverDto save(DriverDto dto) throws RepositoryException {
        if (dto == null) {
            return null;
        }

        if (driverDao != null) {
            driverDao.update(dto);
            return dto;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DriverDAOImpl dao = new DriverDAOImpl(connection);
            dao.update(dto);
            return dto;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save driver", e);
        }
    }

    @Override
    public Optional<DriverDto> findById(String id) throws RepositoryException {
        if (id == null) {
            return Optional.empty();
        }

        if (driverDao != null) {
            return Optional.ofNullable(driverDao.findbyId(id));
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DriverDAOImpl dao = new DriverDAOImpl(connection);
            return Optional.ofNullable(dao.findbyId(id));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find driver", e);
        }
    }

    @Override
    public List<DriverDto> findAll() throws RepositoryException {
        if (driverDao != null) {
            return driverDao.findAll();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DriverDAOImpl dao = new DriverDAOImpl(connection);
            return dao.findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load drivers", e);
        }
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null) {
            return;
        }

        if (driverDao != null) {
            driverDao.delete(id);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DriverDAOImpl dao = new DriverDAOImpl(connection);
            dao.delete(id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete driver", e);
        }
    }
}