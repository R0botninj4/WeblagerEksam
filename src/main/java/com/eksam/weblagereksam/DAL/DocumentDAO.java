package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentDAO implements IDocumentDAO {

    private final DBConnector dbConnector;

    public DocumentDAO() throws Exception {
        dbConnector = new DBConnector();
    }
    public UUID addDocument(Document document) {
        String sql = """
                INSERT INTO Documents (BoxId, DocumentNumber, BarcodeValue, Status)
                OUTPUT INSERTED.Id
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, document.getBoxId().toString());
            stmt.setInt(2, document.getDocumentNumber());
            stmt.setString(3, document.getBarcodeValue());
            stmt.setString(4, document.getStatus());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception e) {
            throw new RuntimeException("Database operation failed.", e);
        }

        return null;
    }
    public List<Document> getDocumentsByBoxId(UUID boxId) {
        List<Document> documents = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Documents
                WHERE BoxId = ?
                ORDER BY DocumentNumber
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, boxId.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("CreatedAt");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

                documents.add(new Document(
                        UUID.fromString(rs.getString("Id")),
                        UUID.fromString(rs.getString("BoxId")),
                        rs.getInt("DocumentNumber"),
                        rs.getString("BarcodeValue"),
                        rs.getString("Status"),
                        createdAt
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Database operation failed.", e);
        }

        return documents;
    }
    public Document getLatestDocumentByBoxId(UUID boxId) {
        String sql = """
                SELECT TOP 1 *
                FROM Documents
                WHERE BoxId = ?
                ORDER BY DocumentNumber DESC
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, boxId.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("CreatedAt");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

                return new Document(
                        UUID.fromString(rs.getString("Id")),
                        UUID.fromString(rs.getString("BoxId")),
                        rs.getInt("DocumentNumber"),
                        rs.getString("BarcodeValue"),
                        rs.getString("Status"),
                        createdAt
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Database operation failed.", e);
        }

        return null;
    }
    public int getNextDocumentNumber(UUID boxId) {
        String sql = """
                SELECT ISNULL(MAX(DocumentNumber), 0) + 1 AS NextNumber
                FROM Documents
                WHERE BoxId = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, boxId.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("NextNumber");
            }

        } catch (Exception e) {
            throw new RuntimeException("Database operation failed.", e);
        }

        return 1;
    }
    public boolean updateDocumentStatus(UUID documentId, String status) {
        String sql = """
                UPDATE Documents
                SET Status = ?
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, documentId.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Database operation failed.", e);
        }
    }
    public boolean deleteDocument(UUID documentId) {
        String sql = "DELETE FROM Documents WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documentId.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Database operation failed.", e);
        }
    }
}
