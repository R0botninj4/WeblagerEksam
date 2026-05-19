package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;

import java.util.ArrayList;
import java.util.HashMap;
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

        for (Page page : pageManager.getPageSummariesByBoxId(boxId)) {
            pagesByDocument.computeIfAbsent(page.getDocumentId(), ignored -> new ArrayList<>()).add(page);
            totalFiles++;
        }

        for (Document document : documentManager.getDocumentsByBoxId(boxId)) {
            if (!pagesByDocument.containsKey(document.getId())) {
                continue;
            }

            documents.add(document);
        }

        UUID documentIdToLoad = selectedDocumentId;
        if (documentIdToLoad == null || !pagesByDocument.containsKey(documentIdToLoad)) {
            documentIdToLoad = documents.isEmpty() ? null : documents.get(0).getId();
        }

        return new BoxDataSnapshot(documents, pagesByDocument, documentIdToLoad, totalFiles);
    }

    public Page loadPage(UUID pageId) {
        return pageManager.getPageById(pageId);
    }

    // ===== Page editing =====

    public boolean rotatePage(Page page, int deltaDegrees) {
        return setPageRotation(page, page.getRotation() + deltaDegrees);
    }

    public boolean setPageRotation(Page page, int rotation) {
        int normalizedRotation = normalizeRotation(rotation);
        page.setRotation(normalizedRotation);
        return pageManager.updatePageRotation(page.getId(), normalizedRotation);
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

    public boolean movePageBetweenDocuments(
            UUID sourceDocumentId,
            UUID targetDocumentId,
            List<Page> sourcePages,
            List<Page> targetPages
    ) throws Exception {
        updatePageNumbers(sourceDocumentId, sourcePages);
        updatePageNumbers(targetDocumentId, targetPages);

        List<Page> changedPages = new ArrayList<>();
        changedPages.addAll(sourcePages);
        changedPages.addAll(targetPages);

        boolean saved = pageManager.updatePageDocumentsAndOrders(changedPages);

        if (saved && sourcePages.isEmpty()) {
            documentManager.deleteDocument(sourceDocumentId);
        }

        return saved;
    }

    private void updatePageNumbers(UUID documentId, List<Page> pages) {
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            page.setDocumentId(documentId);
            page.setUiOrder(i + 1);
        }
    }

    // ===== Small helpers =====

    private int normalizeRotation(int rotation) {
        int normalized = rotation % 360;
        return normalized < 0 ? normalized + 360 : normalized;
    }

    public record BoxDataSnapshot(
            List<Document> documents,
            Map<UUID, List<Page>> pagesByDocument,
            UUID selectedDocumentId,
            int totalFiles
    ) {}
}
