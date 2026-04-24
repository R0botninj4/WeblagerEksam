package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.DAL.DocumentDAO;

import java.util.List;
import java.util.UUID;

/**
 * BLL manager for document operations.
 *
 * Documents belong to boxes and contain pages. This class keeps document-related
 * logic out of GUI controllers.
 */
public class DocumentManager {

    // ===== DAL dependency =====

    private final DocumentDAO documentDAO;

    public DocumentManager() throws Exception {
        documentDAO = new DocumentDAO();
    }

    // ===== Read methods =====

    public List<Document> getDocumentsByBoxId(UUID boxId) {
        return documentDAO.getDocumentsByBoxId(boxId);
    }

    /**
     * Used during scanning so new pages can continue in the latest open document.
     */
    public Document getLatestDocumentByBoxId(UUID boxId) {
        return documentDAO.getLatestDocumentByBoxId(boxId);
    }

    // ===== Write methods =====

    public boolean updateDocumentStatus(UUID documentId, String status) {
        return documentDAO.updateDocumentStatus(documentId, status);
    }

    public boolean deleteDocument(UUID documentId) {
        return documentDAO.deleteDocument(documentId);
    }
}
