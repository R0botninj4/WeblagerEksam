package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Page;

import java.util.List;
import java.util.UUID;

public interface IPageDAO {

    UUID addPage(Page page);

    List<Page> getPagesByDocumentId(UUID documentId);

    Page getPageById(UUID pageId);

    List<Page> getPageSummariesByBoxId(UUID boxId);

    int getNextReferenceScanOrder(UUID documentId);

    int getNextUiOrder(UUID documentId);

    boolean updatePage(Page page);

    boolean updatePageRotation(UUID pageId, int rotation);

    boolean deletePage(UUID pageId);

    boolean updatePageOrders(UUID documentId, List<Page> pages);
}
