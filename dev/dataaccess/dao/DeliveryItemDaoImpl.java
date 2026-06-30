package dataaccess.dao;

import dataaccess.dto.DeliveryItemDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryItemDaoImpl implements DaoInterface<DeliveryItemDto> {
    private final Connection connection;

    public DeliveryItemDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DeliveryItemDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO delivery_items (item_id, document_number, item_name, quantity) VALUES (?, ?, ?, ?) ON CONFLICT(item_id, document_number) DO UPDATE SET item_name = excluded.item_name, quantity = excluded.quantity
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dto.getItemId());
            stmt.setInt(2, dto.getDocumentNumber());
            stmt.setString(3, dto.getItemName());
            stmt.setInt(4, dto.getQuantity());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save delivery_items row", e);
        }
    }

    @Override
    public DeliveryItemDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT item_id, document_number, item_name, quantity
                FROM delivery_items
                WHERE item_id = ? AND document_number = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setString(1, parts[0]);
            stmt.setInt(2, Integer.parseInt(parts[1]));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find delivery_items row", e);
        }
    }

    @Override
    public void update(DeliveryItemDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM delivery_items WHERE item_id = ? AND document_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = id.split(":", 2);
            if (parts.length != 2) { throw new RepositoryException("Invalid composite id: " + id); }
            stmt.setString(1, parts[0]);
            stmt.setInt(2, Integer.parseInt(parts[1]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete delivery_items row", e);
        }
    }

    @Override
    public List<DeliveryItemDto> findAll() throws RepositoryException {
        String sql = """
                SELECT item_id, document_number, item_name, quantity
                FROM delivery_items
                """;
        List<DeliveryItemDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load delivery_items rows", e);
        }
    }

    private DeliveryItemDto mapRow(ResultSet rs) throws SQLException {
        return new DeliveryItemDto(
                rs.getString("item_id"),
                rs.getInt("document_number"),
                rs.getString("item_name"),
                rs.getInt("quantity")
        );
    }
}
