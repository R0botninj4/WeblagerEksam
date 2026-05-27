package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.LogEntry;
import com.eksam.weblagereksam.DAL.ILogDAO;
import com.eksam.weblagereksam.DAL.LogDAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LogManager {

    private final ILogDAO logDAO;
    private static final int LOG_TIME_TO_LIVE_DAYS = 90;

    public LogManager() throws Exception {
        logDAO = new LogDAO();
    }

    public List<LogEntry> getAllLogs() {
        // BLL method used by admin.
        // It keeps the GUI away from SQL and database code.
        LocalDateTime oldestAllowedLog = LocalDateTime.now().minusDays(LOG_TIME_TO_LIVE_DAYS);
        return logDAO.getAllLogs().stream()
                .filter(log -> log.getCreatedAt() == null || !log.getCreatedAt().isBefore(oldestAllowedLog))
                .toList();
    }

    public void createLog(UUID userId, String action, String tableName, UUID recordId, String oldValue, String newValue) {
        // This creates an audit log entry.
        // Example: user X opened box Y at this time.
        logDAO.createLog(userId, action, tableName, recordId, oldValue, newValue);
    }
}
