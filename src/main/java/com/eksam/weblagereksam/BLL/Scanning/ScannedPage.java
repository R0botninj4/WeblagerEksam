package com.eksam.weblagereksam.BLL.Scanning;

public class ScannedPage {

    private final byte[] imageData;
    private final String checksum;
    private final String barcodeValue;
    private final int width;
    private final int height;

    public ScannedPage(byte[] imageData, String checksum, String barcodeValue, int width, int height) {
        this.imageData = imageData;
        this.checksum = checksum;
        this.barcodeValue = barcodeValue;
        this.width = width;
        this.height = height;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public long getFileSize() {
        return imageData.length;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getBarcodeValue() {
        return barcodeValue;
    }

    public boolean isBarcodePage() {
        return barcodeValue != null && !barcodeValue.isBlank();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
