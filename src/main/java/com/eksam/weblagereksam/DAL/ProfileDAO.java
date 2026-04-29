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
                Timestamp createdTimestamp = rs.getTimestamp("CreatedAt");
                LocalDateTime createdAt = createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null;

                profiles.add(new Profile(
                        UUID.fromString(rs.getString("Id")),
                        UUID.fromString(rs.getString("ClientId")),
                        rs.getString("ClientName"),
                        rs.getString("Name"),
                        rs.getString("BarcodeSplitRule"),
                        rs.getString("MetadataSchema"),
                        createdAt
                ));
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
                SET ClientId = ?, Name = ?, BarcodeSplitRule = ?, MetadataSchema = ?
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profile.getClientId().toString());
            stmt.setString(2, profile.getName());
            stmt.setString(3, profile.getBarcodeSplitRule());
            stmt.setString(4, profile.getMetadataSchema());
            stmt.setString(5, profile.getId().toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteProfile(UUID id) {
        String sql = "DELETE FROM Profiles WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
