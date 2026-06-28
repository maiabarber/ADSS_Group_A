package dataaccess.dao;

import dataaccess.dto.DeliveryDocumentDto;
import dataaccess.repository.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryDocumentDaoImpl implements DaoInterface<DeliveryDocumentDto> {
    private final Connection connection;

    public DeliveryDocumentDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DeliveryDocumentDto dto) throws RepositoryException {
        String sql = """
                INSERT INTO delivery_documents (document_number, stop_id) VALUES (?, ?) ON CONFLICT(document_number) DO UPDATE SET stop_id = excluded.stop_id
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dto.getDocumentNumber());
            stmt.setInt(2, dto.getStopId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save delivery_documents row", e);
        }
    }

    @Override
    public DeliveryDocumentDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT document_number, stop_id
                FROM delivery_documents
                WHERE document_number = ?
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
            throw new RepositoryException("Failed to find delivery_documents row", e);
        }
    }

    @Override
    public void update(DeliveryDocumentDto dto) throws RepositoryException {
        createOrUpdate(dto);
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM delivery_documents WHERE document_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String[] parts = new String[] { id };
            stmt.setInt(1, Integer.parseInt(parts[0]));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete delivery_documents row", e);
        }
    }

    @Override
    public List<DeliveryDocumentDto> findAll() throws RepositoryException {
        String sql = """
                SELECT document_number, stop_id
                FROM delivery_documents
                """;
        List<DeliveryDocumentDto> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load delivery_documents rows", e);
        }
    }

    private DeliveryDocumentDto mapRow(ResultSet rs) throws SQLException {
        return new DeliveryDocumentDto(
                rs.getInt("document_number"),
                rs.getInt("stop_id")
        );
    }
}
