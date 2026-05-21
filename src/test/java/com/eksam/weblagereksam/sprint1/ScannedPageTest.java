package com.eksam.weblagereksam.sprint1;

import com.eksam.weblagereksam.BLL.Scanning.ScannedPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScannedPageTest {

    @Test
    void sprint1Scanning_pageWithBarcodeIsDetectedAsBarcodePage() {
        ScannedPage page = new ScannedPage(new byte[]{1, 2, 3}, "checksum", "4743110", 100, 200);

        assertTrue(page.isBarcodePage());
    }

    @Test
    void sprint1Scanning_pageWithoutBarcodeIsNormalPage() {
        ScannedPage page = new ScannedPage(new byte[]{1, 2, 3}, "checksum", "", 100, 200);

        assertFalse(page.isBarcodePage());
    }

    @Test
    void sprint1Scanning_fileSizeComesFromImageBytes() {
        ScannedPage page = new ScannedPage(new byte[]{10, 20, 30, 40}, "checksum", null, 100, 200);

        assertEquals(4, page.getFileSize());
        assertEquals(100, page.getWidth());
        assertEquals(200, page.getHeight());
    }
}
