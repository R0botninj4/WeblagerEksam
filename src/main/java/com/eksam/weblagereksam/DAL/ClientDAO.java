package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientDAO implements IClientDAO {

    private final DBConnector dbConnector;

    public ClientDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Clients
                ORDER BY Name
                """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapClient(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }

    public List<Client> getActiveClients() {
        List<Client> clients = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Clients
                WHERE IsActive = 1
                ORDER BY Name
                """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapClient(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }

    public UUID addClient(Client client) {
        String sql = """
                INSERT INTO Clients (Name)
                OUTPUT INSERTED.Id
                VALUES (?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getName());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception e) {
            throw new RuntimeException("Client could not be created in the database.", e);
        }

        return null;
    }

    public boolean updateClient(Client client) {
        String sql = """
                UPDATE Clients
                SET Name = ?, IsActive = ?
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getName());
            stmt.setBoolean(2, client.isActive());
            stmt.setString(3, client.getId().toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deactivateClient(UUID id) {
        String sql = "UPDATE Clients SET IsActive = 0 WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean activateClient(UUID id) {
        String sql = "UPDATE Clients SET IsActive = 1 WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        Timestamp createdTimestamp = rs.getTimestamp("CreatedAt");
        LocalDateTime createdAt = createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null;

        return new Client(
                UUID.fromString(rs.getString("Id")),
                rs.getString("Name"),
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
