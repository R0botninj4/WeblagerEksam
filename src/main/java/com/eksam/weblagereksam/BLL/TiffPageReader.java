package com.eksam.weblagereksam.BLL;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * BLL service for splitting a TIFF file into separate image pages.
 */
public class TiffPageReader {

    /**
     * Reads all pages from a TIFF byte array.
     *
     * Some TIFF files contain more than one page, so scanning must handle every page separately.
     */
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

                // Read every page in the multi-page TIFF.
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
