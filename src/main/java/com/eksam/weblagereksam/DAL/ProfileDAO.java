package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileDAO implements IProfileDAO {

    private final DBConnector dbConnector;

    public ProfileDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    public List<Profile> getAllProfiles() {
        List<Profile> profiles = new ArrayList<>();

        String sql = """
                SELECT p.*, c.Name AS ClientName
                FROM Profiles p
                INNER JOIN Clients c ON p.ClientId = c.Id
                ORDER BY c.Name, p.Name
                """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                profiles.add(mapProfile(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return profiles;
    }

    public List<Profile> getActiveProfiles() {
        List<Profile> profiles = new ArrayList<>();

        String sql = """
                SELECT p.*, c.Name AS ClientName
                FROM Profiles p
                INNER JOIN Clients c ON p.ClientId = c.Id
                WHERE p.IsActive = 1 AND c.IsActive = 1
                ORDER BY c.Name, p.Name
                """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                profiles.add(mapProfile(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return profiles;
    }

    public UUID addProfile(Profile profile) {
        String sql = """
                INSERT INTO Profiles (ClientId, Name, BarcodeSplitRule, MetadataSchema)
                OUTPUT INSERTED.Id
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profile.getClientId().toString());
            stmt.setString(2, profile.getName());
            stmt.setString(3, profile.getBarcodeSplitRule());
            stmt.setString(4, profile.getMetadataSchema());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateProfile(Profile profile) {
        String sql = """
                UPDATE Profiles
                SET ClientId = ?, Name = ?, BarcodeSplitRule = ?, MetadataSchema = ?, IsActive = ?
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profile.getClientId().toString());
            stmt.setString(2, profile.getName());
            stmt.setString(3, profile.getBarcodeSplitRule());
            stmt.setString(4, profile.getMetadataSchema());
            stmt.setBoolean(5, profile.isActive());
            stmt.setString(6, profile.getId().toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deactivateProfile(UUID id) {
        String sql = "UPDATE Profiles SET IsActive = 0 WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deactivateProfilesByClientId(UUID clientId) {
        String sql = "UPDATE Profiles SET IsActive = 0 WHERE ClientId = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clientId.toString());
            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean activateProfile(UUID id) {
        String sql = "UPDATE Profiles SET IsActive = 1 WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Profile mapProfile(ResultSet rs) throws SQLException {
        Timestamp createdTimestamp = rs.getTimestamp("CreatedAt");
        LocalDateTime createdAt = createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null;

        return new Profile(
                UUID.fromString(rs.getString("Id")),
                UUID.fromString(rs.getString("ClientId")),
                rs.getString("ClientName"),
                rs.getString("Name"),
                rs.getString("BarcodeSplitRule"),
                rs.getString("MetadataSchema"),
                hasColumn(rs, "IsActive") ? rs.getBoolean("IsActive") : true,
                createdAt
        );
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }
}
