package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.LogEntry;

import java.util.List;
import java.util.UUID;

public interface ILogDAO {

    List<LogEntry> getAllLogs();

    void createLog(UUID userId, String action, String tableName, UUID recordId, String oldValue, String newValue);
}
