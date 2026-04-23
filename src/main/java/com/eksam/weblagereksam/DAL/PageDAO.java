package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PageDAO implements IPageDAO {

    private final DBConnector dbConnector;

    public PageDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    @Override
    public UUID addPage(Page page) {
        String sql = """
                INSERT INTO Pages
                (DocumentId, ReferenceScanOrder, UiOrder, FileName, MimeType, ImageData,
                 FileSize, Checksum, Rotation, Width, Height, IsBarcodePage)
                OUTPUT INSERTED.Id
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, page.getDocumentId().toString());
            stmt.setInt(2, page.getReferenceScanOrder());
            stmt.setInt(3, page.getUiOrder());
            stmt.setString(4, page.getFileName());
            stmt.setString(5, page.getMimeType());
            stmt.setBytes(6, page.getImageData());

            if (page.getFileSize() != null) {
                stmt.setLong(7, page.getFileSize());
            } else {
                stmt.setNull(7, Types.BIGINT);
            }

            stmt.setString(8, page.getChecksum());
            stmt.setInt(9, page.getRotation());

            if (page.getWidth() != null) {
                stmt.setInt(10, page.getWidth());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            if (page.getHeight() != null) {
                stmt.setInt(11, page.getHeight());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }

            stmt.setBoolean(12, page.isBarcodePage());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Page> getPagesByDocumentId(UUID documentId) {
        List<Page> pages = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Pages
                WHERE DocumentId = ?
                ORDER BY UiOrder
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documentId.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("CreatedAt");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

                Long fileSize = rs.getObject("FileSize") != null ? rs.getLong("FileSize") : null;
                Integer width = rs.getObject("Width") != null ? rs.getInt("Width") : null;
                Integer height = rs.getObject("Height") != null ? rs.getInt("Height") : null;

                pages.add(new Page(
                        UUID.fromString(rs.getString("Id")),
                        UUID.fromString(rs.getString("DocumentId")),
                        rs.getInt("ReferenceScanOrder"),
                        rs.getInt("UiOrder"),
                        rs.getString("FileName"),
                        rs.getString("MimeType"),
                        rs.getBytes("ImageData"),
                        fileSize,
                        rs.getString("Checksum"),
                        rs.getInt("Rotation"),
                        width,
                        height,
                        rs.getBoolean("IsBarcodePage"),
                        createdAt
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pages;
    }

    @Override
    public int getNextReferenceScanOrder(UUID documentId) {
        String sql = """
                SELECT ISNULL(MAX(ReferenceScanOrder), 0) + 1 AS NextReferenceScanOrder
                FROM Pages
                WHERE DocumentId = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documentId.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("NextReferenceScanOrder");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    @Override
    public int getNextUiOrder(UUID documentId) {
        String sql = """
                SELECT ISNULL(MAX(UiOrder), 0) + 1 AS NextUiOrder
                FROM Pages
                WHERE DocumentId = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documentId.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("NextUiOrder");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    @Override
    public boolean updatePage(Page page) {
        String sql = """
                UPDATE Pages
                SET DocumentId = ?,
                    ReferenceScanOrder = ?,
                    UiOrder = ?,
                    FileName = ?,
                    MimeType = ?,
                    ImageData = ?,
                    FileSize = ?,
                    Checksum = ?,
                    Rotation = ?,
                    Width = ?,
                    Height = ?,
                    IsBarcodePage = ?
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, page.getDocumentId().toString());
            stmt.setInt(2, page.getReferenceScanOrder());
            stmt.setInt(3, page.getUiOrder());
            stmt.setString(4, page.getFileName());
            stmt.setString(5, page.getMimeType());
            stmt.setBytes(6, page.getImageData());

            if (page.getFileSize() != null) {
                stmt.setLong(7, page.getFileSize());
            } else {
                stmt.setNull(7, Types.BIGINT);
            }

            stmt.setString(8, page.getChecksum());
            stmt.setInt(9, page.getRotation());

            if (page.getWidth() != null) {
                stmt.setInt(10, page.getWidth());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            if (page.getHeight() != null) {
                stmt.setInt(11, page.getHeight());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }

            stmt.setBoolean(12, page.isBarcodePage());
            stmt.setString(13, page.getId().toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deletePage(UUID pageId) {
        String sql = "DELETE FROM Pages WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pageId.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updatePageOrders(UUID documentId, List<Page> pages) {
        String sql = """
                UPDATE Pages
                SET UiOrder = ?
                WHERE Id = ? AND DocumentId = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Page page : pages) {
                stmt.setInt(1, page.getUiOrder());
                stmt.setString(2, page.getId().toString());
                stmt.setString(3, documentId.toString());
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
