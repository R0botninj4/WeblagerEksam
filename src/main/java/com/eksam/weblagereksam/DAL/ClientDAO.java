package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
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
}
