package dataaccess.repository.impl;

import dataaccess.DatabaseInitializer;
import dataaccess.dao.SubmissionDeadlineDAO;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.SubmissionDeadlineRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class DatabaseSubmissionDeadlineRepository implements SubmissionDeadlineRepository {
    private final SubmissionDeadlineDAO dao = new SubmissionDeadlineDAO();

    public DatabaseSubmissionDeadlineRepository() {
        ensureSchema();
    }

    @Override
    public void save(LocalDate deadline) throws RepositoryException {
        try {
            dao.save(deadline);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save submission deadline", e);
        }
    }

    @Override
    public Optional<LocalDate> findCurrent() throws RepositoryException {
        try {
            return dao.findCurrent();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to read submission deadline", e);
        }
    }

    private void ensureSchema() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }
}
