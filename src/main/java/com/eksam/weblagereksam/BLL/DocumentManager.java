package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.DAL.DocumentDAO;

import java.util.List;
import java.util.UUID;

public class DocumentManager {

    private final DocumentDAO documentDAO;

    public DocumentManager() throws Exception {
        documentDAO = new DocumentDAO();
    }

    public List<Document> getDocumentsByBoxId(UUID boxId) {
        return documentDAO.getDocumentsByBoxId(boxId);
    }

    public Document getLatestDocumentByBoxId(UUID boxId) {
        return documentDAO.getLatestDocumentByBoxId(boxId);
    }

    public boolean updateDocumentStatus(UUID documentId, String status) {
        return documentDAO.updateDocumentStatus(documentId, status);
    }

    public boolean deleteDocument(UUID documentId) {
        return documentDAO.deleteDocument(documentId);
    }
}
