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
                Timestamp createdTimestamp = rs.getTimestamp("CreatedAt");
                LocalDateTime createdAt = createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null;

                clients.add(new Client(
                        UUID.fromString(rs.getString("Id")),
                        rs.getString("Name"),
                        rs.getString("Code"),
                        createdAt
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }

    public UUID addClient(Client client) {
        String sql = """
                INSERT INTO Clients (Name, Code)
                OUTPUT INSERTED.Id
                VALUES (?, ?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getName());
            stmt.setString(2, client.getCode());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateClient(Client client) {
        String sql = """
                UPDATE Clients
                SET Name = ?, Code = ?
                WHERE Id = ?
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getName());
            stmt.setString(2, client.getCode());
            stmt.setString(3, client.getId().toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteClient(UUID id) {
        String sql = "DELETE FROM Clients WHERE Id = ?";

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
