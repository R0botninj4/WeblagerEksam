package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.DocumentMetadata;
import com.eksam.weblagereksam.DAL.DocumentMetadataDAO;
import com.eksam.weblagereksam.DAL.IDocumentMetadataDAO;

import java.util.List;
import java.util.UUID;

public class DocumentMetadataManager {

    private final IDocumentMetadataDAO documentMetadataDAO;

    public DocumentMetadataManager() throws Exception {
        documentMetadataDAO = new DocumentMetadataDAO();
    }

    public List<DocumentMetadata> getMetadataByDocumentId(UUID documentId) {
        return documentMetadataDAO.getMetadataByDocumentId(documentId);
    }
}
