package dataaccess.dao;

import dataaccess.dto.ShiftDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShiftDaoImpl implements DaoInterface<ShiftDto> {
    private final Connection connection;

    public ShiftDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(ShiftDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO shifts (shift_id, shift_date, shift_type, branch_id) VALUES (?, ?, ?, ?) ON CONFLICT(shift_id) DO UPDATE SET shift_date = excluded.shift_date, shift_type = excluded.shift_type, branch_id = excluded.branch_id
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getShiftId());
            stmt.setString(2, dto.getShiftDate());
            stmt.setString(3, dto.getShiftType());
            if (dto.getBranchId() == null) { stmt.setNull(4, Types.INTEGER); } else { stmt.setInt(4, dto.getBranchId()); }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save shifts row", e);
        }
    }

    @Override
    public ShiftDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT shift_id, shift_date, shift_type, branch_id
                FROM shifts
                WHERE shift_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find shifts row", e);
        }
    }

    @Override
    public void update(ShiftDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM shifts WHERE shift_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete shifts row", e);
        }
    }

    @Override
    public List<ShiftDto> findAll() throws RepositoryException {
        String sql = """
                SELECT shift_id, shift_date, shift_type, branch_id
                FROM shifts
                """;
        List<ShiftDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load shifts rows", e);
        }
    }

    private ShiftDto mapRow(ResultSet rs) throws SQLException {
        return new ShiftDto(
                rs.getInt("shift_id"),
                rs.getString("shift_date"),
                rs.getString("shift_type"),
                getNullableInt(rs, "branch_id")
        );
    }

    private Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
