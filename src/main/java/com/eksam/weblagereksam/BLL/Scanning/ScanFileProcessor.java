package com.eksam.weblagereksam.BLL.Scanning;

import com.eksam.weblagereksam.BLL.Image.ImageByteConverter;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class ScanFileProcessor {

    // This class is the "middle step" between the raw TIFF file and the database.
    //
    // The API gives us TIFF bytes. Those bytes are not nice to work with directly,
    // so this class opens the TIFF file, reads every page, checks each page for a
    // barcode, and returns simple ScannedPage objects.
    //
    // This keeps ScanImportManager easier to read because it does not need to know
    // about BufferedImage, barcode libraries or image conversion.

    private final TiffPageReader tiffPageReader;
    private final BarcodeReaderService barcodeReaderService;

    public ScanFileProcessor() {
        tiffPageReader = new TiffPageReader();
        barcodeReaderService = new BarcodeReaderService();
    }

    public List<ScannedPage> process(byte[] tiffBytes) throws Exception {
        List<ScannedPage> scannedPages = new ArrayList<>();

        // One TIFF file can contain several pages.
        // That is why we first split the TIFF into normal Java images and then
        // handle each page one at a time.
        for (BufferedImage image : tiffPageReader.readAllPages(tiffBytes)) {
            byte[] imageData = ImageByteConverter.bufferedImageToTiffBytes(image);

            // For each page we save:
            // imageData: the actual TIFF bytes that go into the database.
            // checksum: a fingerprint of the image, useful for checking changes.
            // barcode: if this page contains a barcode, it starts a new document.
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

    // SHA-256 creates a text value from the image bytes.
    // If two pages have the same bytes, they get the same checksum.
    // If the image is rotated or changed, the checksum changes too.
    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}
