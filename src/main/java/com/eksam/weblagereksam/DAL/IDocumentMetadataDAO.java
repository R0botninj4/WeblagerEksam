package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.DocumentMetadata;

import java.util.List;
import java.util.UUID;

public interface IDocumentMetadataDAO {

    List<DocumentMetadata> getMetadataByDocumentId(UUID documentId);
}
