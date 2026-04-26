package com.eksam.weblagereksam.BLL.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImageByteConverter {

    // ===== Byte conversion =====

    public static byte[] bufferedImageToTiffBytes(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (!ImageIO.write(image, "TIFF", baos)) {
            throw new Exception("No TIFF writer found.");
        }

        return baos.toByteArray();
    }

    public static BufferedImage bytesToBufferedImage(byte[] imageBytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    // ===== Image rotation =====

    public static BufferedImage rotate(BufferedImage source, int degrees) {
        int normalizedDegrees = ((degrees % 360) + 360) % 360;

        if (normalizedDegrees == 0) {
            return source;
        }

        boolean swapDimensions = normalizedDegrees == 90 || normalizedDegrees == 270;
        int targetWidth = swapDimensions ? source.getHeight() : source.getWidth();
        int targetHeight = swapDimensions ? source.getWidth() : source.getHeight();

        BufferedImage rotated = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rotated.createGraphics();

        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, targetWidth, targetHeight);

            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            AffineTransform transform = new AffineTransform();
            transform.translate(targetWidth / 2.0, targetHeight / 2.0);
            transform.rotate(Math.toRadians(normalizedDegrees));
            transform.translate(-source.getWidth() / 2.0, -source.getHeight() / 2.0);
            graphics.drawRenderedImage(source, transform);
        } finally {
            graphics.dispose();
        }

        return rotated;
    }
}
