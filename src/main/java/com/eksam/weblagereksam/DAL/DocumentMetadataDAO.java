package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.DocumentMetadata;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentMetadataDAO implements IDocumentMetadataDAO {

    private final DBConnector dbConnector;

    public DocumentMetadataDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    public List<DocumentMetadata> getMetadataByDocumentId(UUID documentId) {
        List<DocumentMetadata> metadata = new ArrayList<>();

        String sql = """
                SELECT *
                FROM DocumentMetadata
                WHERE DocumentId = ?
                ORDER BY FieldName
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documentId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                metadata.add(new DocumentMetadata(
                        UUID.fromString(rs.getString("Id")),
                        UUID.fromString(rs.getString("DocumentId")),
                        rs.getString("FieldName"),
                        rs.getString("FieldValue")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return metadata;
    }
}
