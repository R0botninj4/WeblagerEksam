package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Document;

import java.util.List;
import java.util.UUID;

public interface IDocumentDAO {

    UUID addDocument(Document document);

    List<Document> getDocumentsByBoxId(UUID boxId);

    int getNextDocumentNumber(UUID boxId);
}