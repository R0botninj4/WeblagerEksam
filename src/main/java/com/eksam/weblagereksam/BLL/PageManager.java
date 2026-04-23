package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.DAL.PageDAO;

import java.util.List;
import java.util.UUID;

public class PageManager {

    private final PageDAO pageDAO;

    public PageManager() throws Exception {
        pageDAO = new PageDAO();
    }

    public List<Page> getPagesByDocumentId(UUID documentId) {
        return pageDAO.getPagesByDocumentId(documentId);
    }

    public int getNextReferenceScanOrder(UUID documentId) {
        return pageDAO.getNextReferenceScanOrder(documentId);
    }

    public int getNextUiOrder(UUID documentId) {
        return pageDAO.getNextUiOrder(documentId);
    }

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
