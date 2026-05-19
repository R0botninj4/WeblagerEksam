package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.DAL.IPageDAO;
import com.eksam.weblagereksam.DAL.PageDAO;

import java.util.List;
import java.util.UUID;

public class PageManager {

    // ===== DAL dependency =====

    private final IPageDAO pageDAO;

    public PageManager() throws Exception {
        pageDAO = new PageDAO();
    }

    // ===== Read methods =====

    public List<Page> getPagesByDocumentId(UUID documentId) {
        return pageDAO.getPagesByDocumentId(documentId);
    }

    public Page getPageById(UUID pageId) {
        return pageDAO.getPageById(pageId);
    }

    public List<Page> getPageSummariesByBoxId(UUID boxId) {
        return pageDAO.getPageSummariesByBoxId(boxId);
    }

    public boolean updatePage(Page page) {
        return pageDAO.updatePage(page);
    }

    public boolean updatePageRotation(UUID pageId, int rotation) {
        return pageDAO.updatePageRotation(pageId, rotation);
    }

    public boolean deletePage(UUID pageId) {
        return pageDAO.deletePage(pageId);
    }

    public boolean updatePageOrders(UUID documentId, List<Page> pages) {
        return pageDAO.updatePageOrders(documentId, pages);
    }

    public boolean updatePageDocumentsAndOrders(List<Page> pages) {
        return pageDAO.updatePageDocumentsAndOrders(pages);
    }
}
