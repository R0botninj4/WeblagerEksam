package com.eksam.weblagereksam.BLL;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * GUI/BLL helper for converting database image bytes into JavaFX Image objects.
 *
 * JavaFX ImageView cannot display raw database bytes directly.
 */
public class FxImageConverter {

    /**
     * Converts stored page image bytes into an ImageView-friendly JavaFX Image.
     */
    public static Image bytesToFxImage(byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) {
                return null;
            }

            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (bufferedImage == null) {
                System.out.println("Could not decode image bytes.");
                return null;
            }

            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
