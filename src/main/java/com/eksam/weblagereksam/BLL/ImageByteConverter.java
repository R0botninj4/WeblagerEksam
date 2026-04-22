package com.eksam.weblagereksam.BLL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ImageByteConverter {

    public static byte[] bufferedImageToTiffBytes(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "TIFF", baos);
        return baos.toByteArray();
    }
}