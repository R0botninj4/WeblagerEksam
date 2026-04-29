package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
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
}
