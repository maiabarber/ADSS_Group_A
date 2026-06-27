package dataaccess.dao;

import dataaccess.dto.UserDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements DaoInterface<UserDto> {

    private final Connection connection;

    public UserDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(UserDto user) throws RepositoryException {
        String sql = """
                INSERT OR REPLACE INTO users (
                    user_id,
                    password,
                    is_hr_manager
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getPassword());
            stmt.setInt(3, user.isHRManager() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save user", e);
        }
    }

    @Override
    public UserDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT user_id, password, is_hr_manager
                FROM users
                WHERE user_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return buildUserDto(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find user by id", e);
        }
    }

    @Override
    public void update(UserDto user) throws RepositoryException {
        createOrUpdate(user);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete user", e);
        }
    }

    @Override
    public List<UserDto> findAll() throws RepositoryException {
        String sql = """
                SELECT user_id, password, is_hr_manager
                FROM users
                ORDER BY user_id
                """;

        List<UserDto> users = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(buildUserDto(rs));
            }

            return users;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load users", e);
        }
    }

    private UserDto buildUserDto(ResultSet rs) throws SQLException {
        return new UserDto(
                rs.getString("user_id"),
                rs.getString("password"),
                rs.getInt("is_hr_manager") == 1
        );
    }
}