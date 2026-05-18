package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class LogEntry {

    private final UUID id;
    private final String username;
    private final String action;
    private final String tableName;
    private final String oldValue;
    private final String newValue;
    private final LocalDateTime createdAt;

    public LogEntry(UUID id,
                    String username,
                    String action,
                    String tableName,
                    String oldValue,
                    String newValue,
                    LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.tableName = tableName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public String getUsername() { return username; }

    public String getAction() { return action; }

    public String getTableName() { return tableName; }

    public String getOldValue() { return oldValue; }

    public String getNewValue() { return newValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
