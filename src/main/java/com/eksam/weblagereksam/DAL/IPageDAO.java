package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Page;

import java.util.List;
import java.util.UUID;

public interface IPageDAO {

    UUID addPage(Page page);

    List<Page> getPagesByDocumentId(UUID documentId);

    int getNextReferenceScanOrder(UUID documentId);

    int getNextUiOrder(UUID documentId);

    boolean updatePage(Page page);

    boolean deletePage(UUID pageId);

    boolean updatePageOrders(UUID documentId, List<Page> pages);
}
