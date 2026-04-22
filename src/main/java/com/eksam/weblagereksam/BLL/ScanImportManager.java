package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.DAL.DocumentDAO;
import com.eksam.weblagereksam.DAL.PageDAO;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScanImportManager {

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

    public int importRandomTiffToBox(UUID boxId) throws Exception {
        byte[] tiffBytes = tiffApiClient.getRandomTiffBytes();
        return importTiffBytesToBox(boxId, List.of(tiffBytes));
    }

    public int importRandomTiffBatchToBox(UUID boxId, int amount) throws Exception {
        List<byte[]> tiffFiles = tiffApiClient.getRandomTiffBatch(amount);
        return importTiffBytesToBox(boxId, tiffFiles, null);
    }

    public int importRandomTiffBatchToBox(UUID boxId, int amount, ProgressListener progressListener) throws Exception {
        List<byte[]> tiffFiles = tiffApiClient.getRandomTiffBatch(amount);
        return importTiffBytesToBox(boxId, tiffFiles, progressListener);
    }

    private int importTiffBytesToBox(UUID boxId, List<byte[]> tiffFiles) throws Exception {
        return importTiffBytesToBox(boxId, tiffFiles, null);
    }

    private int importTiffBytesToBox(UUID boxId, List<byte[]> tiffFiles, ProgressListener progressListener) throws Exception {
        if (progressListener != null) {
            progressListener.onProgress(0, Math.max(1, tiffFiles.size()), "Preparing scans...");
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
            List<ProcessedPage> processedPages = preprocessPages(images);

            for (ProcessedPage processedPage : processedPages) {
                boolean isBarcodePage = processedPage.barcodeValue() != null && !processedPage.barcodeValue().isBlank();

                if (isBarcodePage) {
                    currentDocumentId = createDocument(boxId, currentDocumentNumber, processedPage.barcodeValue());
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
                        buildFileName(importBatchId, currentDocumentNumber - 1, currentReferenceScanOrder, isBarcodePage),
                        "image/png",
                        processedPage.pageBytes(),
                        (long) processedPage.pageBytes().length,
                        processedPage.checksum(),
                        0,
                        processedPage.width(),
                        processedPage.height(),
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
                        "Processed file " + (fileIndex + 1) + " of " + tiffFiles.size()
                );
            }
        }

        return importedDocuments;
    }

    private List<ProcessedPage> preprocessPages(List<BufferedImage> images) throws Exception {
        int poolSize = Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), images.size()));
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        try {
            List<Future<ProcessedPage>> futures = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                final int referenceScanOrder = i + 1;
                final BufferedImage image = images.get(i);

                futures.add(executor.submit(() -> {
                    String barcodeValue = barcodeReaderService.readBarcode(image);
                    byte[] pageBytes = ImageByteConverter.bufferedImageToPngBytes(image);

                    return new ProcessedPage(
                            referenceScanOrder,
                            barcodeValue,
                            pageBytes,
                            sha256(pageBytes),
                            image.getWidth(),
                            image.getHeight()
                    );
                }));
            }

            List<ProcessedPage> processedPages = new ArrayList<>();
            for (Future<ProcessedPage> future : futures) {
                processedPages.add(future.get());
            }

            return processedPages;
        } catch (ExecutionException e) {
            throw new Exception("Could not preprocess TIFF pages.", e.getCause());
        } finally {
            executor.shutdown();
        }
    }

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

    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }

    private String buildFileName(String importBatchId, int documentNumber, int referenceScanOrder, boolean isBarcodePage) {
        String prefix = isBarcodePage ? "barcode" : "page";
        return prefix + "-" + importBatchId + "-doc-" + documentNumber + "-ref-" + referenceScanOrder + ".png";
    }

    private record ProcessedPage(
            int referenceScanOrder,
            String barcodeValue,
            byte[] pageBytes,
            String checksum,
            int width,
            int height
    ) {}

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int completed, int total, String message);
    }
}
