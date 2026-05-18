package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.LogEntry;
import com.eksam.weblagereksam.DAL.ILogDAO;
import com.eksam.weblagereksam.DAL.LogDAO;

import java.util.List;
import java.util.UUID;

public class LogManager {

    private final ILogDAO logDAO;

    public LogManager() throws Exception {
        logDAO = new LogDAO();
    }

    public List<LogEntry> getAllLogs() {
        // BLL method used by admin.
        // It keeps the GUI away from SQL and database code.
        return logDAO.getAllLogs();
    }

    public void createLog(UUID userId, String action, String tableName, UUID recordId, String oldValue, String newValue) {
        // This creates an audit log entry.
        // Example: user X opened box Y at this time.
        logDAO.createLog(userId, action, tableName, recordId, oldValue, newValue);
    }
}
