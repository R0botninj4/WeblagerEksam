package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.LogEntry;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LogDAO implements ILogDAO {

    private final DBConnector dbConnector;

    public LogDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    public List<LogEntry> getAllLogs() {
        List<LogEntry> logs = new ArrayList<>();

        String sql = """
                SELECT TOP 500 l.*, u.Username
                FROM Logs l
                LEFT JOIN Users u ON l.UserId = u.Id
                ORDER BY l.CreatedAt DESC
                """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapLog(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }

    public void createLog(UUID userId, String action, String tableName, UUID recordId, String oldValue, String newValue) {
        String sql = """
                INSERT INTO Logs (UserId, Action, TableName, RecordId, OldValue, NewValue)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId == null ? null : userId.toString());
            stmt.setString(2, action);
            stmt.setString(3, tableName);
            stmt.setString(4, recordId == null ? null : recordId.toString());
            stmt.setString(5, oldValue);
            stmt.setString(6, newValue);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LogEntry mapLog(ResultSet rs) throws Exception {
        Timestamp createdTimestamp = rs.getTimestamp("CreatedAt");
        LocalDateTime createdAt = createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null;

        String recordIdText = rs.getString("RecordId");
        UUID recordId = recordIdText == null ? null : UUID.fromString(recordIdText);

        return new LogEntry(
                UUID.fromString(rs.getString("Id")),
                rs.getString("Username"),
                rs.getString("Action"),
                rs.getString("TableName"),
                recordId,
                rs.getString("OldValue"),
                rs.getString("NewValue"),
                createdAt
        );
    }
}
