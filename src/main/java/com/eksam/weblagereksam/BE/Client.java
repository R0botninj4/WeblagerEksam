package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Client {

    private UUID id;
    private String name;
    private String code;
    private boolean active;
    private LocalDateTime createdAt;

    public Client(UUID id, String name, String code, LocalDateTime createdAt) {
        this(id, name, code, true, createdAt);
    }

    public Client(UUID id, String name, String code, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public String getCode() { return code; }

    public boolean isActive() { return active; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }

    public void setCode(String code) { this.code = code; }

    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name;
    }
}
