package com.eksam.weblagereksam.BLL.Scanning;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TiffPageReader {

    // Reads every page inside a TIFF file.
    //
    // A TIFF file can be a single image, but it can also contain many pages.
    // That is why we do not just read "one image". We ask ImageIO how many
    // pages/images are inside the TIFF and then read them all.
    public List<BufferedImage> readAllPages(byte[] tiffBytes) throws Exception {
        List<BufferedImage> pages = new ArrayList<>();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(tiffBytes);
             ImageInputStream iis = ImageIO.createImageInputStream(bais)) {

            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIFF");

            if (!readers.hasNext()) {
                throw new Exception("No TIFF reader found.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(iis);
                int count = reader.getNumImages(true);

                // Convert each TIFF page into a BufferedImage.
                // BufferedImage is Java's normal image type, and it lets us rotate,
                // display and check the page for barcodes.
                for (int i = 0; i < count; i++) {
                    pages.add(reader.read(i));
                }
            } finally {
                reader.dispose();
            }
        }

        return pages;
    }
}
