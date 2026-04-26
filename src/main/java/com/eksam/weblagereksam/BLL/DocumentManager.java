package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.DAL.DocumentDAO;

import java.util.List;
import java.util.UUID;

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

    public boolean deleteDocument(UUID documentId) {
        return documentDAO.deleteDocument(documentId);
    }
}
