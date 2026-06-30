package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.ShippingZoneDaoImpl;
import dataaccess.dto.ShippingZoneDto;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ShippingZoneRepositoryImpl implements Repository<ShippingZoneDto, String> {
    private final ShippingZoneDaoImpl shippingZoneDao;

    public ShippingZoneRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize ShippingZoneRepository", e);
        }

        this.shippingZoneDao = null;
    }

    public ShippingZoneRepositoryImpl(ShippingZoneDaoImpl shippingZoneDao) {
        this.shippingZoneDao = shippingZoneDao;
    }

    @Override
    public ShippingZoneDto save(ShippingZoneDto dto) throws RepositoryException {
        if (dto == null) {
            return null;
        }

        if (shippingZoneDao != null) {
            shippingZoneDao.update(dto);
            return dto;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            ShippingZoneDaoImpl dao = new ShippingZoneDaoImpl(connection);
            dao.update(dto);
            return dto;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save shipping zone", e);
        }
    }

    @Override
    public Optional<ShippingZoneDto> findById(String id) throws RepositoryException {
        if (id == null) {
            return Optional.empty();
        }

        if (shippingZoneDao != null) {
            return Optional.ofNullable(shippingZoneDao.findbyId(id));
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            ShippingZoneDaoImpl dao = new ShippingZoneDaoImpl(connection);
            return Optional.ofNullable(dao.findbyId(id));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shipping zone", e);
        }
    }

    @Override
    public List<ShippingZoneDto> findAll() throws RepositoryException {
        if (shippingZoneDao != null) {
            return shippingZoneDao.findAll();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            ShippingZoneDaoImpl dao = new ShippingZoneDaoImpl(connection);
            return dao.findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load shipping zones", e);
        }
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null) {
            return;
        }

        if (shippingZoneDao != null) {
            shippingZoneDao.delete(id);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            ShippingZoneDaoImpl dao = new ShippingZoneDaoImpl(connection);
            dao.delete(id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete shipping zone", e);
        }
    }
}