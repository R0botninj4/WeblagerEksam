package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Profile {

    private UUID id;
    private UUID clientId;
    private String clientName;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;

    public Profile(UUID id,
                   UUID clientId,
                   String clientName,
                   String name,
                   LocalDateTime createdAt) {
        this(id, clientId, clientName, name, true, createdAt);
    }

    public Profile(UUID id,
                   UUID clientId,
                   String clientName,
                   String name,
                   boolean active,
                   LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.name = name;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public UUID getClientId() { return clientId; }

    public String getClientName() { return clientName; }

    public String getName() { return name; }

    public boolean isActive() { return active; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setClientId(UUID clientId) { this.clientId = clientId; }

    public void setClientName(String clientName) { this.clientName = clientName; }

    public void setName(String name) { this.name = name; }

    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return clientName == null || clientName.isBlank()
                ? name
                : name + " (" + clientName + ")";
    }
}
