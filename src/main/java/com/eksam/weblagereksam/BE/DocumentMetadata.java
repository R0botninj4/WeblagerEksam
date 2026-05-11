package com.eksam.weblagereksam.BE;

import java.util.UUID;

public class DocumentMetadata {

    private UUID id;
    private UUID documentId;
    private String fieldName;
    private String fieldValue;

    public DocumentMetadata(UUID id, UUID documentId, String fieldName, String fieldValue) {
        this.id = id;
        this.documentId = documentId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }
}
