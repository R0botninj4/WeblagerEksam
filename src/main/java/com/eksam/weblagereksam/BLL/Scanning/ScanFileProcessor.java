package com.eksam.weblagereksam.BLL.Scanning;

import com.eksam.weblagereksam.BLL.Image.ImageByteConverter;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class ScanFileProcessor {

    private final TiffPageReader tiffPageReader;
    private final BarcodeReaderService barcodeReaderService;

    public ScanFileProcessor() {
        tiffPageReader = new TiffPageReader();
        barcodeReaderService = new BarcodeReaderService();
    }

    public List<ScannedPage> process(byte[] tiffBytes) throws Exception {
        List<ScannedPage> scannedPages = new ArrayList<>();

        for (BufferedImage image : tiffPageReader.readAllPages(tiffBytes)) {
            byte[] imageData = ImageByteConverter.bufferedImageToTiffBytes(image);

            scannedPages.add(new ScannedPage(
                    imageData,
                    sha256(imageData),
                    barcodeReaderService.readBarcode(image),
                    image.getWidth(),
                    image.getHeight()
            ));
        }

        return scannedPages;
    }

    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}
