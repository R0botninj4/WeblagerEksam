package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.BLL.Scanning.ScanFileProcessor;
import com.eksam.weblagereksam.BLL.Scanning.ScannedPage;
import com.eksam.weblagereksam.BLL.Scanning.TiffApiClient;
import com.eksam.weblagereksam.DAL.DocumentDAO;
import com.eksam.weblagereksam.DAL.IDocumentDAO;
import com.eksam.weblagereksam.DAL.IPageDAO;
import com.eksam.weblagereksam.DAL.PageDAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ScanImportManager {

    // ===== DAL and processing services =====

    private final IDocumentDAO documentDAO;
    private final IPageDAO pageDAO;
    private final TiffApiClient tiffApiClient;
    private final ScanFileProcessor scanFileProcessor;

    public ScanImportManager() throws Exception {
        documentDAO = new DocumentDAO();
        pageDAO = new PageDAO();
        tiffApiClient = new TiffApiClient();
        scanFileProcessor = new ScanFileProcessor();
    }

    // ===== Public import methods =====

    public int importRandomTiffToBox(UUID boxId) throws Exception {
        byte[] tiffBytes = tiffApiClient.getRandomTiffBytes();
        return importTiffBytesToBox(boxId, List.of(tiffBytes), null);
    }

    public int importRandomTiffBatchToBox(UUID boxId, int amount, ProgressListener progressListener) throws Exception {
        List<byte[]> tiffFiles = tiffApiClient.getRandomTiffBatch(amount);
        return importTiffBytesToBox(boxId, tiffFiles, progressListener);
    }

    // ===== Import pipeline =====

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

            for (ScannedPage scannedPage : scanFileProcessor.process(tiffBytes)) {
                if (scannedPage.isBarcodePage()) {
                    currentDocumentId = createDocument(boxId, currentDocumentNumber, scannedPage.getBarcodeValue());
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

                Page page = new Page(
                        null,
                        currentDocumentId,
                        currentReferenceScanOrder,
                        currentUiOrder,
                        buildFileName(importBatchId, currentDocumentNumber - 1, currentReferenceScanOrder, scannedPage.isBarcodePage()),
                        "image/tiff",
                        scannedPage.getImageData(),
                        scannedPage.getFileSize(),
                        scannedPage.getChecksum(),
                        0,
                        scannedPage.getWidth(),
                        scannedPage.getHeight(),
                        scannedPage.isBarcodePage(),
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

    private String buildFileName(String importBatchId, int documentNumber, int referenceScanOrder, boolean isBarcodePage) {
        String prefix = isBarcodePage ? "barcode" : "page";
        return prefix + "-" + importBatchId + "-doc-" + documentNumber + "-ref-" + referenceScanOrder + ".tiff";
    }

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int completed, int total, String message);
    }
}
