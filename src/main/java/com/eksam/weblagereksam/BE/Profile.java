package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Profile {

    private UUID id;
    private UUID clientId;
    private String clientName;
    private String name;
    private String barcodeSplitRule;
    private String metadataSchema;
    private LocalDateTime createdAt;

    public Profile(UUID id,
                   UUID clientId,
                   String clientName,
                   String name,
                   String barcodeSplitRule,
                   String metadataSchema,
                   LocalDateTime createdAt) {

        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.name = name;
        this.barcodeSplitRule = barcodeSplitRule;
        this.metadataSchema = metadataSchema;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public UUID getClientId() { return clientId; }

    public String getClientName() { return clientName; }

    public String getName() { return name; }

    public String getBarcodeSplitRule() { return barcodeSplitRule; }

    public String getMetadataSchema() { return metadataSchema; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setClientId(UUID clientId) { this.clientId = clientId; }

    public void setClientName(String clientName) { this.clientName = clientName; }

    public void setName(String name) { this.name = name; }

    public void setBarcodeSplitRule(String barcodeSplitRule) { this.barcodeSplitRule = barcodeSplitRule; }

    public void setMetadataSchema(String metadataSchema) { this.metadataSchema = metadataSchema; }

    @Override
    public String toString() {
        return clientName == null || clientName.isBlank()
                ? name
                : name + " (" + clientName + ")";
    }
}
