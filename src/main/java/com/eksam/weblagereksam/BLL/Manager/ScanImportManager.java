package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.BLL.Service.BarcodeReaderService;
import com.eksam.weblagereksam.BLL.Service.TiffApiClient;
import com.eksam.weblagereksam.BLL.Service.TiffPageReader;
import com.eksam.weblagereksam.BLL.Util.ImageByteConverter;
import com.eksam.weblagereksam.DAL.DocumentDAO;
import com.eksam.weblagereksam.DAL.PageDAO;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public class ScanImportManager {

    // ===== DAL and processing services =====

    private final DocumentDAO documentDAO;
    private final PageDAO pageDAO;
    private final TiffApiClient tiffApiClient;
    private final TiffPageReader tiffPageReader;
    private final BarcodeReaderService barcodeReaderService;

    public ScanImportManager() throws Exception {
        documentDAO = new DocumentDAO();
        pageDAO = new PageDAO();
        tiffApiClient = new TiffApiClient();
        tiffPageReader = new TiffPageReader();
        barcodeReaderService = new BarcodeReaderService();
    }

    // ===== Public import methods =====

    public int importRandomTiffToBox(UUID boxId) throws Exception {
        byte[] tiffBytes = tiffApiClient.getRandomTiffBytes();
        return importTiffBytesToBox(boxId, List.of(tiffBytes));
    }

    public int importRandomTiffBatchToBox(UUID boxId, int amount, ProgressListener progressListener) throws Exception {
        List<byte[]> tiffFiles = tiffApiClient.getRandomTiffBatch(amount);
        return importTiffBytesToBox(boxId, tiffFiles, progressListener);
    }

    // ===== Import pipeline =====

    private int importTiffBytesToBox(UUID boxId, List<byte[]> tiffFiles) throws Exception {
        return importTiffBytesToBox(boxId, tiffFiles, null);
    }

    private int importTiffBytesToBox(UUID boxId, List<byte[]> tiffFiles, ProgressListener progressListener) throws Exception {
        if (progressListener != null) {
            progressListener.onProgress(0, Math.max(1, tiffFiles.size()), "Getting ready...");
        }
        String importBatchId = UUID.randomUUID().toString().substring(0, 8);

        Document currentDocument = documentDAO.getLatestDocumentByBoxId(boxId);
        UUID currentDocumentId = currentDocument != null ? currentDocument.getId() : null;
        int currentDocumentNumber = documentDAO.getNextDocumentNumber(boxId);
        int currentReferenceScanOrder = currentDocumentId != null ? pageDAO.getNextReferenceScanOrder(currentDocumentId) : 1;
        int currentUiOrder = currentDocumentId != null ? pageDAO.getNextUiOrder(currentDocumentId) : 1;
        int importedDocuments = 0;

        for (int fileIndex = 0; fileIndex < tiffFiles.size(); fileIndex++) {
            byte[] tiffBytes = tiffFiles.get(fileIndex);
            List<BufferedImage> images = tiffPageReader.readAllPages(tiffBytes);

            for (BufferedImage image : images) {
                String barcodeValue = barcodeReaderService.readBarcode(image);
                boolean isBarcodePage = barcodeValue != null && !barcodeValue.isBlank();

                if (isBarcodePage) {
                    currentDocumentId = createDocument(boxId, currentDocumentNumber, barcodeValue);
                    currentDocumentNumber++;
                    importedDocuments++;
                    currentReferenceScanOrder = pageDAO.getNextReferenceScanOrder(currentDocumentId);
                    currentUiOrder = pageDAO.getNextUiOrder(currentDocumentId);
                }

                if (currentDocumentId == null) {
                    currentDocumentId = createDocument(boxId, currentDocumentNumber, null);
                    currentDocumentNumber++;
                    currentReferenceScanOrder = 1;
                    currentUiOrder = 1;
                    importedDocuments++;
                }

                byte[] pageBytes = ImageByteConverter.bufferedImageToTiffBytes(image);

                Page page = new Page(
                        null,
                        currentDocumentId,
                        currentReferenceScanOrder,
                        currentUiOrder,
                        buildFileName(importBatchId, currentDocumentNumber - 1, currentReferenceScanOrder, isBarcodePage),
                        "image/tiff",
                        pageBytes,
                        (long) pageBytes.length,
                        sha256(pageBytes),
                        0,
                        image.getWidth(),
                        image.getHeight(),
                        isBarcodePage,
                        LocalDateTime.now()
                );

                pageDAO.addPage(page);
                currentReferenceScanOrder++;
                currentUiOrder++;
            }

            if (progressListener != null) {
                progressListener.onProgress(
                        fileIndex + 1,
                        Math.max(1, tiffFiles.size()),
                        "File " + (fileIndex + 1) + " of " + tiffFiles.size()
                );
            }
        }

        return importedDocuments;
    }

    // ===== Database helpers =====

    private UUID createDocument(UUID boxId, int documentNumber, String barcodeValue) throws Exception {
        Document document = new Document(
                null,
                boxId,
                documentNumber,
                barcodeValue,
                "SCANNED",
                LocalDateTime.now()
        );

        UUID documentId = documentDAO.addDocument(document);

        if (documentId == null) {
            throw new Exception("Could not create document.");
        }

        return documentId;
    }

    // ===== Metadata helpers =====

    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }

    private String buildFileName(String importBatchId, int documentNumber, int referenceScanOrder, boolean isBarcodePage) {
        String prefix = isBarcodePage ? "barcode" : "page";
        return prefix + "-" + importBatchId + "-doc-" + documentNumber + "-ref-" + referenceScanOrder + ".tiff";
    }

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int completed, int total, String message);
    }
}
