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
    public void createOrUpdate(UserDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO users (user_id, password, is_hr_manager) VALUES (?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET password = excluded.password, is_hr_manager = excluded.is_hr_manager
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getUserId());
            stmt.setString(2, dto.getPassword());
            stmt.setInt(3, dto.isHrManager() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save users row", e);
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
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find users row", e);
        }
    }

    @Override
    public void update(UserDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setString(1, parts[0]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete users row", e);
        }
    }

    @Override
    public List<UserDto> findAll() throws RepositoryException {
        String sql = """
                SELECT user_id, password, is_hr_manager
                FROM users
                """;
        List<UserDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load users rows", e);
        }
    }

    private UserDto mapRow(ResultSet rs) throws SQLException {
        return new UserDto(
                rs.getString("user_id"),
                rs.getString("password"),
                rs.getInt("is_hr_manager") == 1
        );
    }
}
