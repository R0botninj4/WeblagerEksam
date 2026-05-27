package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.DAL.DocumentDAO;
import com.eksam.weblagereksam.DAL.IDocumentDAO;

import java.util.List;
import java.util.UUID;

public class DocumentManager {

    // ===== DAL dependency =====

    private final IDocumentDAO documentDAO;

    public DocumentManager() throws Exception {
        documentDAO = new DocumentDAO();
    }

    // ===== Read methods =====

    public List<Document> getDocumentsByBoxId(UUID boxId) {
        return documentDAO.getDocumentsByBoxId(boxId);
    }

    public UUID createDocument(UUID boxId, int documentNumber, String barcodeValue) {
        Document document = new Document(null, boxId, documentNumber, barcodeValue, "SCANNED", null);
        return documentDAO.addDocument(document);
    }

    public int getNextDocumentNumber(UUID boxId) {
        return documentDAO.getNextDocumentNumber(boxId);
    }

    public boolean deleteDocument(UUID documentId) {
        return documentDAO.deleteDocument(documentId);
    }
}
