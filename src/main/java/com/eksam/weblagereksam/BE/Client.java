package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Client {

    private UUID id;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;

    public Client(UUID id, String name, LocalDateTime createdAt) {
        this(id, name, true, createdAt);
    }

    public Client(UUID id, String name, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public boolean isActive() { return active; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }

    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name;
    }
}
