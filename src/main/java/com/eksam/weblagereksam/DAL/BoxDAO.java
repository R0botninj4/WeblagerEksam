package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoxDAO implements IBoxDAO {

    private final DBConnector dbConnector;

    public BoxDAO() throws Exception {
        dbConnector = new DBConnector();
    }
    public List<Box> getAllBoxes() {
        List<Box> boxes = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Boxes
                ORDER BY BoxNumber
                """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                boxes.add(mapBox(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return boxes;
    }
    public List<Box> getBoxesByUserId(UUID userId) {
        List<Box> boxes = new ArrayList<>();

        String sql = """
                SELECT b.*
                FROM Boxes b
                INNER JOIN UserBoxes ub ON ub.BoxId = b.Id
                WHERE ub.UserId = ?
                ORDER BY b.BoxNumber
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boxes.add(mapBox(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return boxes;
    }
    public Box getBoxById(UUID id) {
        String sql = """
                SELECT *
                FROM Boxes
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapBox(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public Box getBoxByBoxNumber(String boxNumber) {
        String sql = """
                SELECT *
                FROM Boxes
                WHERE BoxNumber = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, boxNumber);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapBox(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public UUID addBox(Box box) {
        String sql = """
                INSERT INTO Boxes (ClientId, ProfileId, BoxNumber, Label, Status)
                OUTPUT INSERTED.Id
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, box.getClientId().toString());

            if (box.getProfileId() != null) {
                stmt.setString(2, box.getProfileId().toString());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setString(3, box.getBoxNumber());
            stmt.setString(4, box.getLabel());
            stmt.setString(5, box.getStatus());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public boolean updateBox(Box box) {
        String sql = """
                UPDATE Boxes
                SET ClientId = ?,
                    ProfileId = ?,
                    BoxNumber = ?,
                    Label = ?,
                    Status = ?
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, box.getClientId().toString());

            if (box.getProfileId() != null) {
                stmt.setString(2, box.getProfileId().toString());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setString(3, box.getBoxNumber());
            stmt.setString(4, box.getLabel());
            stmt.setString(5, box.getStatus());
            stmt.setString(6, box.getId().toString());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean deleteBox(UUID id) {
        String sql = "DELETE FROM Boxes WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Box mapBox(ResultSet rs) throws SQLException {
        Timestamp createdTimestamp = rs.getTimestamp("CreatedAt");
        LocalDateTime createdAt = createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null;

        String profileIdString = rs.getString("ProfileId");
        UUID profileId = profileIdString != null ? UUID.fromString(profileIdString) : null;

        return new Box(
                UUID.fromString(rs.getString("Id")),
                UUID.fromString(rs.getString("ClientId")),
                profileId,
                rs.getString("BoxNumber"),
                rs.getString("Label"),
                rs.getString("Status"),
                createdAt
        );
    }
}
