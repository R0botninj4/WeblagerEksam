package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Document {

    private UUID id;
    private UUID boxId;
    private int documentNumber;
    private String barcodeValue;
    private String status;
    private LocalDateTime createdAt;

    public Document(UUID id, UUID boxId, int documentNumber, String barcodeValue, String status, LocalDateTime createdAt) {
        this.id = id;
        this.boxId = boxId;
        this.documentNumber = documentNumber;
        this.barcodeValue = barcodeValue;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoxId() {
        return boxId;
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public String getBarcodeValue() {
        return barcodeValue;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setBoxId(UUID boxId) {
        this.boxId = boxId;
    }

    public void setDocumentNumber(int documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setBarcodeValue(String barcodeValue) {
        this.barcodeValue = barcodeValue;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}