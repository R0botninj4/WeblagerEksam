package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScanWorkspaceManager {

    // ===== BLL managers =====

    private final DocumentManager documentManager;
    private final PageManager pageManager;

    public ScanWorkspaceManager() throws Exception {
        documentManager = new DocumentManager();
        pageManager = new PageManager();
    }

    // ===== Loading screen data =====

    public BoxDataSnapshot loadBoxData(UUID boxId, UUID selectedDocumentId) {
        List<Document> documents = new ArrayList<>();
        Map<UUID, List<Page>> pagesByDocument = new HashMap<>();
        int totalFiles = 0;

        for (Document document : documentManager.getDocumentsByBoxId(boxId)) {
            List<Page> pages = pageManager.getPagesByDocumentId(document.getId());

            if (pages.isEmpty()) {
                continue;
            }

            documents.add(document);
            pagesByDocument.put(document.getId(), new ArrayList<>(pages));
            totalFiles += pages.size();
        }

        return new BoxDataSnapshot(documents, pagesByDocument, selectedDocumentId, totalFiles);
    }

    // ===== Page editing =====

    public boolean rotatePage(Page page, int deltaDegrees) throws Exception {
        BufferedImage source = ImageByteConverter.bytesToBufferedImage(page.getImageData());
        BufferedImage rotated = ImageByteConverter.rotate(source, deltaDegrees);
        byte[] imageBytes = ImageByteConverter.bufferedImageToTiffBytes(rotated);

        page.setImageData(imageBytes);
        page.setFileSize((long) imageBytes.length);
        page.setFileName(ensureTiffFileName(page.getFileName()));
        page.setMimeType("image/tiff");
        page.setWidth(rotated.getWidth());
        page.setHeight(rotated.getHeight());
        page.setRotation(normalizeRotation(page.getRotation() + deltaDegrees));
        page.setChecksum(sha256(imageBytes));

        return pageManager.updatePage(page);
    }

    public boolean deletePage(Page page, UUID documentId, List<Page> remainingPages) throws Exception {
        if (!pageManager.deletePage(page.getId())) {
            return false;
        }

        if (remainingPages.isEmpty()) {
            return documentManager.deleteDocument(documentId);
        }

        return updatePageOrders(documentId, remainingPages);
    }

    public boolean updatePageOrders(UUID documentId, List<Page> pages) {
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).setUiOrder(i + 1);
        }

        return pageManager.updatePageOrders(documentId, pages);
    }

    // ===== Small helpers =====

    private String ensureTiffFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "page.tiff";
        }

        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return fileName + ".tiff";
        }

        return fileName.substring(0, extensionIndex) + ".tiff";
    }

    private int normalizeRotation(int rotation) {
        int normalized = rotation % 360;
        return normalized < 0 ? normalized + 360 : normalized;
    }

    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }

    public record BoxDataSnapshot(
            List<Document> documents,
            Map<UUID, List<Page>> pagesByDocument,
            UUID selectedDocumentId,
            int totalFiles
    ) {}
}
