package dataaccess.repository.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.DeliveryDAOImpl;
import dataaccess.dto.DeliveryDto;
import dataaccess.repository.DeliveryRepository;
import dataaccess.repository.RepositoryException;

public class DeliveryRepositoryImpl implements DeliveryRepository {
    private final DeliveryDAOImpl deliveryDao;

    public DeliveryRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize DeliveryRepository", e);
        }

        this.deliveryDao = null;
    }

    public DeliveryRepositoryImpl(DeliveryDAOImpl deliveryDao) {
        this.deliveryDao = deliveryDao;
    }

    @Override
    public DeliveryDto save(DeliveryDto dto) throws RepositoryException {
        if (dto == null) {
            return null;
        }

        if (deliveryDao != null) {
            deliveryDao.update(dto);
            return dto;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DeliveryDAOImpl dao = new DeliveryDAOImpl(connection);
            dao.update(dto);
            return dto;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save delivery", e);
        }
    }
    
    @Override
    public Optional<DeliveryDto> findById(Integer id) throws RepositoryException {
        if (id == null) {
            return Optional.empty();
        }

        if (deliveryDao != null) {
            return Optional.ofNullable(deliveryDao.findbyId(String.valueOf(id)));
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DeliveryDAOImpl dao = new DeliveryDAOImpl(connection);
            return Optional.ofNullable(dao.findbyId(String.valueOf(id)));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find delivery", e);
        }
    }

    @Override
    public List<DeliveryDto> findAll() throws RepositoryException {
        if (deliveryDao != null) {
            return deliveryDao.findAll();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DeliveryDAOImpl dao = new DeliveryDAOImpl(connection);
            return dao.findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load deliveries", e);
        }
    }

    @Override
    public void deleteById(Integer id) throws RepositoryException {
        if (id == null) {
            return;
        }

        if (deliveryDao != null) {
            deliveryDao.delete(String.valueOf(id));
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            DeliveryDAOImpl dao = new DeliveryDAOImpl(connection);
            dao.delete(String.valueOf(id));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete delivery", e);
        }
    }
}