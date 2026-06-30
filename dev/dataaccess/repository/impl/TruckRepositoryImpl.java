package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.TruckDAO;
import dataaccess.dto.TruckDto;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.TruckRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TruckRepositoryImpl implements TruckRepository {
    private final TruckDAO truckDao;

    public TruckRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize TruckRepository", e);
        }

        this.truckDao = null;
    }

    public TruckRepositoryImpl(TruckDAO truckDao) {
        this.truckDao = truckDao;
    }

    @Override
    public TruckDto save(TruckDto dto) throws RepositoryException {
        if (dto == null) {
            return null;
        }

        if (truckDao != null) {
            truckDao.update(dto);
            return dto;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            TruckDAO dao = new TruckDAO(connection);
            dao.update(dto);
            return dto;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save truck", e);
        }
    }

    @Override
    public Optional<TruckDto> findById(String id) throws RepositoryException {
        if (id == null) {
            return Optional.empty();
        }

        if (truckDao != null) {
            return Optional.ofNullable(truckDao.findbyId(id));
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            TruckDAO dao = new TruckDAO(connection);
            return Optional.ofNullable(dao.findbyId(id));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find truck", e);
        }
    }

    @Override
    public List<TruckDto> findAll() throws RepositoryException {
        if (truckDao != null) {
            return truckDao.findAll();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            TruckDAO dao = new TruckDAO(connection);
            return dao.findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load trucks", e);
        }
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null) {
            return;
        }

        if (truckDao != null) {
            truckDao.delete(id);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            TruckDAO dao = new TruckDAO(connection);
            dao.delete(id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete truck", e);
        }
    }
}