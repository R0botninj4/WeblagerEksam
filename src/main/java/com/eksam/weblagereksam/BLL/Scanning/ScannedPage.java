package com.eksam.weblagereksam.BLL.Scanning;

public class ScannedPage {

    // This is a simple data object for one scanned page.
    //
    // After ScanFileProcessor has read a TIFF page, it puts the useful information
    // into this class. That means the rest of the program can work with a simple
    // object instead of dealing with BufferedImage and barcode reading directly.

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
        // If the page has a barcode value, we treat it as a split page.
        // In the scanning flow that means:
        // barcode page found = create/start a new document.
        return barcodeValue != null && !barcodeValue.isBlank();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
