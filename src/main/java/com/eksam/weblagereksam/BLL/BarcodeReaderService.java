package com.eksam.weblagereksam.BLL;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;

/**
 * BLL service for detecting barcodes in scanned pages.
 *
 * A barcode means that scanning should start a new document.
 */
public class BarcodeReaderService {

    /**
     * Tries to read a barcode from the image.
     *
     * The scanner/API can return rotated pages, so the same image is tested in four orientations.
     */
    public String readBarcode(BufferedImage image) {
        int[] rotations = {0, 90, 180, 270};

        for (int rotation : rotations) {
            try {
                BufferedImage candidate = rotation == 0 ? image : ImageByteConverter.rotate(image, rotation);
                LuminanceSource source = new BufferedImageLuminanceSource(candidate);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = new MultiFormatReader().decode(bitmap);
                return result.getText();
            } catch (NotFoundException ignored) {
                // Try the next orientation before giving up.
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }
}
