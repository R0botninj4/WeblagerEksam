package com.eksam.weblagereksam.BE;

import java.time.LocalDateTime;
import java.util.UUID;

public class Page {

    private UUID id;
    private UUID documentId;
    private int referenceScanOrder;
    private int uiOrder;
    private String fileName;
    private String mimeType;
    private byte[] imageData;
    private Long fileSize;
    private String checksum;
    private int rotation;
    private Integer width;
    private Integer height;
    private boolean barcodePage;
    private LocalDateTime createdAt;

    public Page(UUID id, UUID documentId, int referenceScanOrder, int uiOrder,
                String fileName, String mimeType, byte[] imageData, Long fileSize,
                String checksum, int rotation, Integer width, Integer height,
                boolean barcodePage, LocalDateTime createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.referenceScanOrder = referenceScanOrder;
        this.uiOrder = uiOrder;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.imageData = imageData;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.rotation = rotation;
        this.width = width;
        this.height = height;
        this.barcodePage = barcodePage;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public int getReferenceScanOrder() {
        return referenceScanOrder;
    }

    public int getUiOrder() {
        return uiOrder;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public int getRotation() {
        return rotation;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public boolean isBarcodePage() {
        return barcodePage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public void setReferenceScanOrder(int referenceScanOrder) {
        this.referenceScanOrder = referenceScanOrder;
    }

    public void setUiOrder(int uiOrder) {
        this.uiOrder = uiOrder;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setBarcodePage(boolean barcodePage) {
        this.barcodePage = barcodePage;
    }
}