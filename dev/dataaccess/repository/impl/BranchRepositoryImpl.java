package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.BranchDAOImpl;
import dataaccess.dto.BranchDto;
import dataaccess.repository.BranchRepository;
import dataaccess.repository.RepositoryException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BranchRepositoryImpl implements BranchRepository {
    private final BranchDAOImpl dao;

    public BranchRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize BranchRepository", e);
        }
        this.dao = null;
    }

    public BranchRepositoryImpl(BranchDAOImpl dao) {
        this.dao = dao;
    }

    @Override
    public BranchDto save(BranchDto entity) throws RepositoryException {
        if (dao != null) {
            dao.createOrUpdate(entity);
            return entity;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            new BranchDAOImpl(connection).createOrUpdate(entity);
            return entity;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save branch", e);
        }
    }

    @Override
    public Optional<BranchDto> findById(Integer id) throws RepositoryException {
        if (dao != null) {
            return Optional.ofNullable(dao.findbyId(String.valueOf(id)));
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            return Optional.ofNullable(new BranchDAOImpl(connection).findbyId(String.valueOf(id)));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find branch", e);
        }
    }

    @Override
    public List<BranchDto> findAll() throws RepositoryException {
        if (dao != null) {
            return dao.findAll();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            return new BranchDAOImpl(connection).findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load branches", e);
        }
    }

    @Override
    public void deleteById(Integer id) throws RepositoryException {
        if (dao != null) {
            dao.delete(String.valueOf(id));
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            new BranchDAOImpl(connection).delete(String.valueOf(id));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete branch", e);
        }
    }
}
