package com.eksam.weblagereksam.BLL.Image;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class FxImageConverter {

    // Converts saved image bytes into a JavaFX Image.
    //
    // The database stores the scanned page as bytes. JavaFX cannot show raw bytes
    // directly in an ImageView, so we first read the bytes as a normal Java image
    // and then convert it to the JavaFX image type.
    public static Image bytesToFxImage(byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) {
                return null;
            }

            // ImageIO reads the image bytes into BufferedImage.
            // BufferedImage is not for the GUI directly, but it is easy for Java
            // to work with when reading and converting images.
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (bufferedImage == null) {
                System.out.println("Could not decode image bytes.");
                return null;
            }

            // JavaFX cannot show BufferedImage directly.
            // This line converts it into an Image object that can be placed in an ImageView.
            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
