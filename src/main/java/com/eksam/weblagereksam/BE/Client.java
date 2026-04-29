package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Client {

    private UUID id;
    private String name;
    private String code;
    private LocalDateTime createdAt;

    public Client(UUID id, String name, String code, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public String getCode() { return code; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
