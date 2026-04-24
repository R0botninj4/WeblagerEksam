package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.DAL.PageDAO;

import java.util.List;
import java.util.UUID;

/**
 * BLL manager for page operations.
 *
 * Pages are the individual scanned images inside a document.
 */
public class PageManager {

    // ===== DAL dependency =====

    private final PageDAO pageDAO;

    public PageManager() throws Exception {
        pageDAO = new PageDAO();
    }

    // ===== Read methods =====

    public List<Page> getPagesByDocumentId(UUID documentId) {
        return pageDAO.getPagesByDocumentId(documentId);
    }

    /**
     * ReferenceScanOrder is the original scan order and should not change when the user reorders pages.
     */
    public int getNextReferenceScanOrder(UUID documentId) {
        return pageDAO.getNextReferenceScanOrder(documentId);
    }

    /**
     * UiOrder is the visible order in the filmstrip and can change when the user drags pages.
     */
    public int getNextUiOrder(UUID documentId) {
        return pageDAO.getNextUiOrder(documentId);
    }

    // ===== Write methods =====

    public boolean updatePage(Page page) {
        return pageDAO.updatePage(page);
    }

    public boolean deletePage(UUID pageId) {
        return pageDAO.deletePage(pageId);
    }

    public boolean updatePageOrders(UUID documentId, List<Page> pages) {
        return pageDAO.updatePageOrders(documentId, pages);
    }
}
