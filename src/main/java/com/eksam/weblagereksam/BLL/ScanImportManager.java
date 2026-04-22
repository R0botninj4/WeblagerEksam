package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.DAL.DocumentDAO;
import com.eksam.weblagereksam.DAL.PageDAO;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public class ScanImportManager {

    private final DocumentDAO documentDAO;
    private final PageDAO pageDAO;
    private final TiffApiClient tiffApiClient;
    private final TiffPageReader tiffPageReader;

    public ScanImportManager() throws Exception {
        documentDAO = new DocumentDAO();
        pageDAO = new PageDAO();
        tiffApiClient = new TiffApiClient();
        tiffPageReader = new TiffPageReader();
    }

    public UUID importRandomTiffToBox(UUID boxId) throws Exception {
        byte[] tiffBytes = tiffApiClient.getRandomTiffBytes();
        List<BufferedImage> images = tiffPageReader.readAllPages(tiffBytes);

        int nextDocumentNumber = documentDAO.getNextDocumentNumber(boxId);

        Document document = new Document(
                null,
                boxId,
                nextDocumentNumber,
                null,
                "SCANNED",
                LocalDateTime.now()
        );

        UUID documentId = documentDAO.addDocument(document);

        if (documentId == null) {
            throw new Exception("Could not create document.");
        }

        for (int i = 0; i < images.size(); i++) {
            BufferedImage image = images.get(i);
            byte[] pageBytes = ImageByteConverter.bufferedImageToTiffBytes(image);

            Page page = new Page(
                    null,
                    documentId,
                    i + 1,
                    i + 1,
                    "random-from-api.tiff",
                    "image/tiff",
                    pageBytes,
                    (long) pageBytes.length,
                    sha256(pageBytes),
                    0,
                    image.getWidth(),
                    image.getHeight(),
                    false,
                    LocalDateTime.now()
            );

            pageDAO.addPage(page);
        }

        return documentId;
    }

    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}