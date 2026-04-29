package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Box {

    private UUID id;
    private UUID clientId;
    private UUID profileId;
    private String clientName;
    private String profileName;
    private String boxNumber;
    private String label;
    private String status;
    private LocalDateTime createdAt;

    public Box(UUID id, UUID clientId, UUID profileId, String boxNumber,
               String label, String status, LocalDateTime createdAt) {
        this(id, clientId, profileId, null, null, boxNumber, label, status, createdAt);
    }

    public Box(UUID id, UUID clientId, UUID profileId, String clientName, String profileName,
               String boxNumber, String label, String status, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.profileId = profileId;
        this.clientName = clientName;
        this.profileName = profileName;
        this.boxNumber = boxNumber;
        this.label = label;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getBoxNumber() {
        return boxNumber;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
