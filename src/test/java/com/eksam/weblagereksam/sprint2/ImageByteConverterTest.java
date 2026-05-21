package com.eksam.weblagereksam.sprint2;

import com.eksam.weblagereksam.BLL.Image.ImageByteConverter;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ImageByteConverterTest {

    @Test
    void sprint2Rotation_90DegreesSwapsWidthAndHeight() {
        BufferedImage source = createImage(20, 10);

        BufferedImage rotated = ImageByteConverter.rotate(source, 90);

        assertEquals(10, rotated.getWidth());
        assertEquals(20, rotated.getHeight());
    }

    @Test
    void sprint2Rotation_180DegreesKeepsWidthAndHeight() {
        BufferedImage source = createImage(20, 10);

        BufferedImage rotated = ImageByteConverter.rotate(source, 180);

        assertEquals(20, rotated.getWidth());
        assertEquals(10, rotated.getHeight());
    }

    @Test
    void sprint2Rotation_360DegreesReturnsSameImage() {
        BufferedImage source = createImage(20, 10);

        BufferedImage rotated = ImageByteConverter.rotate(source, 360);

        assertSame(source, rotated);
    }

    @Test
    void sprint2Rotation_negative90DegreesWorksLike270Degrees() {
        BufferedImage source = createImage(20, 10);

        BufferedImage rotated = ImageByteConverter.rotate(source, -90);

        assertEquals(10, rotated.getWidth());
        assertEquals(20, rotated.getHeight());
    }

    private BufferedImage createImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().setColor(Color.WHITE);
        image.getGraphics().fillRect(0, 0, width, height);
        return image;
    }
}
