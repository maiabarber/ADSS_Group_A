package dataaccess.repository.impl;

import dataaccess.DatabaseInitializer;
import dataaccess.DatabaseConnection;
import employee.domain.Employee;
import employee.domain.User;
import employee.repository.RepositoryException;
import employee.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseUserRepository implements UserRepository {
    private final DatabaseEmployeeRepository employeeRepository = new DatabaseEmployeeRepository();

    public DatabaseUserRepository() {
        ensureSchema();
    }

    @Override
    public User save(User user) throws RepositoryException {
        if (user == null) {
            throw new RepositoryException("User cannot be null");
        }
        if (user instanceof Employee) {
            employeeRepository.save((Employee) user);
            return user;
        }
        String sql = """
                INSERT INTO users (user_id, password, is_hr_manager)
                VALUES (?, ?, ?)
                ON CONFLICT(user_id) DO UPDATE SET
                    password = excluded.password,
                    is_hr_manager = excluded.is_hr_manager
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId());
            statement.setString(2, user.getPassword());
            statement.setInt(3, user instanceof employee.domain.HR_Manager ? 1 : 0);
            statement.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(String id) throws RepositoryException {
        String sql = "SELECT user_id, password, is_hr_manager FROM users WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = DomainMapper.mapUser(resultSet);
                    if (user instanceof employee.domain.HR_Manager) {
                        return Optional.of(user);
                    }
                    Optional<employee.domain.Employee> employee = employeeRepository.findById(id);
                    return employee.map(value -> (User) value).or(() -> Optional.of(user));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find user", e);
        }
    }

    @Override
    public List<User> findAll() throws RepositoryException {
        String sql = "SELECT user_id, password, is_hr_manager FROM users ORDER BY user_id";
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                User user = DomainMapper.mapUser(resultSet);
                if (user instanceof employee.domain.HR_Manager) {
                    users.add(user);
                } else {
                    users.add(employeeRepository.findById(user.getId()).map(value -> (User) value).orElse(user));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to list users", e);
        }
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            statement.setString(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete user", e);
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
