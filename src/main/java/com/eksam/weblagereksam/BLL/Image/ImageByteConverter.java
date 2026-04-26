package com.eksam.weblagereksam.BLL.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImageByteConverter {

    // ===== Byte conversion =====

    // Converts a BufferedImage into TIFF bytes.
    //
    // We use this when a scanned page has been read or rotated.
    // The result is the actual file content that can be saved in the database.
    public static byte[] bufferedImageToTiffBytes(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (!ImageIO.write(image, "TIFF", baos)) {
            throw new Exception("No TIFF writer found.");
        }

        return baos.toByteArray();
    }

    // Converts image bytes from the database back into a BufferedImage.
    //
    // We need this when the user wants to rotate a page, because rotation is done
    // on a BufferedImage and not directly on the byte array.
    public static BufferedImage bytesToBufferedImage(byte[] imageBytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    // ===== Image rotation =====

    // Rotates the scanned page when the user clicks rotate.
    //
    // This method creates a new image and draws the old image into it with a
    // rotation transform. We return a new image instead of changing the old one.
    public static BufferedImage rotate(BufferedImage source, int degrees) {
        int normalizedDegrees = ((degrees % 360) + 360) % 360;

        if (normalizedDegrees == 0) {
            return source;
        }

        boolean swapDimensions = normalizedDegrees == 90 || normalizedDegrees == 270;
        int targetWidth = swapDimensions ? source.getHeight() : source.getWidth();
        int targetHeight = swapDimensions ? source.getWidth() : source.getHeight();

        // Create a new blank image with the correct size after rotation.
        // For 90 and 270 degrees the width and height must swap places.
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

            // Draw the old image into the new rotated image using the transform above.
            graphics.drawRenderedImage(source, transform);
        } finally {
            graphics.dispose();
        }

        return rotated;
    }
}
