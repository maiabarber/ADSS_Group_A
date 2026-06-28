package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.repository.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SubmissionDeadlineDAOImpl implements DaoInterface<LocalDate> {

    public void save(LocalDate deadline) throws SQLException {
        String deleteSql = "DELETE FROM submissiondeadlines";
        String insertSql = "INSERT INTO submissiondeadlines (deadline_date) VALUES (?)";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement deleteStatement = connection.createStatement();
             PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

            deleteStatement.executeUpdate(deleteSql);
            insertStatement.setString(1, deadline == null ? null : deadline.toString());
            insertStatement.executeUpdate();
        }
    }

    public void deleteCurrent() throws SQLException {
        String sql = "DELETE FROM submissiondeadlines";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public Optional<LocalDate> findCurrent() throws SQLException {
        String sql = "SELECT deadline_date FROM submissiondeadlines ORDER BY rowid DESC LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                String rawDeadline = resultSet.getString("deadline_date");
                return rawDeadline == null ? Optional.empty() : Optional.of(LocalDate.parse(rawDeadline));
            }

            return Optional.empty();
        }
    }

    @Override
    public void createOrUpdate(LocalDate dto) throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createOrUpdate'");
    }

    @Override
    public LocalDate findbyId(String id) throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findbyId'");
    }

    @Override
    public void update(LocalDate dto) throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(String id) throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public List<LocalDate> findAll() throws RepositoryException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }
}