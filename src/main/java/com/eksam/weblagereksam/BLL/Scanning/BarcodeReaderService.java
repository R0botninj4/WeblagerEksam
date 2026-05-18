package com.eksam.weblagereksam.BLL.Scanning;

import com.eksam.weblagereksam.BLL.Image.ImageByteConverter;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;

public class BarcodeReaderService {

    // This method tries to read a barcode from one scanned page.
    // A barcode is important in this project because it tells the program:
    // "Start a new document from here".
    //
    // We test the image in 0, 90, 180 and 270 degrees because paper can be
    // scanned upside down or sideways. If we only checked the original image,
    // some barcodes would not be found.
    public String readBarcode(BufferedImage image) {
        int[] rotations = {0, 90, 180, 270};

        for (int rotation : rotations) {
            try {
                // ZXing is the library that reads the barcode.
                // It cannot read directly from our normal image, so we first convert
                // the page into a black/white bitmap format that ZXing understands.
                BufferedImage candidate = rotation == 0 ? image : ImageByteConverter.rotate(image, rotation);
                LuminanceSource source = new BufferedImageLuminanceSource(candidate);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = new MultiFormatReader().decode(bitmap);
                return result.getText();
            } catch (NotFoundException ignored) {
                // No barcode was found in this rotation.
                // That is not an error, so we just try the next rotation.
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }
}
