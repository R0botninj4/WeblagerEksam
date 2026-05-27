package com.eksam.weblagereksam.GUI.Renderer;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;

public class ScanViewRenderer {

    private static final String FILMSTRIP_THUMB = "filmstrip-thumb";
    private static final String FILMSTRIP_THUMB_ACTIVE = "filmstrip-thumb-active";
    private static final String FILMSTRIP_THUMB_BARCODE = "filmstrip-thumb-barcode";
    private static final String SMALL_TEXT = "small-text";
    private static final String TREE_PAGE_DRAG_PREFIX = "TREE_PAGE|";
    private static final String DOCUMENT_DROP_TARGET = "document-drop-target";
    private static final int DOCUMENT_TREE_ROW_HEIGHT = 36;

    public record DocumentTreeNode(UUID documentId, UUID pageId, int pageIndex, String text) {
        public static DocumentTreeNode root() {
            return new DocumentTreeNode(null, null, -1, "Documents");
        }

        public static DocumentTreeNode document(Document document, List<Page> pages) {
            return new DocumentTreeNode(
                    document.getId(),
                    null,
                    -1,
                    "Document " + document.getDocumentNumber() + " (" + pages.size() + " pages)"
            );
        }

        public static DocumentTreeNode page(Document document, Page page, int pageIndex) {
            String text = page.isBarcodePage() ? "Barcode page" : "Page " + page.getUiOrder();

            if (page.getRotation() != 0) {
                text += " - " + page.getRotation() + " degrees";
            }

            return new DocumentTreeNode(document.getId(), page.getId(), pageIndex, text);
        }

        public boolean isPage() {
            return pageId != null;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    // ===== Document list =====

    public void renderDocumentTree(
            TreeView<DocumentTreeNode> documentTreeView,
            List<Document> documents,
            Map<UUID, List<Page>> pagesByDocument,
            Document selectedDocument,
            int selectedPageIndex,
            PageMoveHandler onPageMoved
    ) {
        TreeItem<DocumentTreeNode> root = new TreeItem<>(DocumentTreeNode.root());
        root.setExpanded(true);

        for (Document document : documents) {
            List<Page> pages = pagesByDocument.getOrDefault(document.getId(), List.of());
            TreeItem<DocumentTreeNode> documentItem = new TreeItem<>(DocumentTreeNode.document(document, pages));
            documentItem.setExpanded(true);

            for (int i = 0; i < pages.size(); i++) {
                documentItem.getChildren().add(new TreeItem<>(DocumentTreeNode.page(document, pages.get(i), i)));
            }

            root.getChildren().add(documentItem);
        }

        documentTreeView.setRoot(root);
        fitTreeHeightToRows(documentTreeView, root);
        setupDocumentTreeDragAndDrop(documentTreeView, onPageMoved);
        selectCurrentTreeItem(documentTreeView, selectedDocument, selectedPageIndex);
    }

    // ===== Filmstrip =====

    public void renderFilmstrip(
            HBox filmstripBox,
            List<Page> pages,
            boolean hasSelectedDocument,
            int currentPageIndex,
            IntConsumer onPageSelected,
            BiFunction<Integer, Integer, Boolean> onPageReordered,
            Map<UUID, VBox> filmstripThumbs
    ) {
        filmstripBox.getChildren().clear();
        filmstripThumbs.clear();

        if (!hasSelectedDocument) {
            Label empty = new Label("No pages");
            empty.getStyleClass().add(SMALL_TEXT);
            filmstripBox.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            VBox thumb = createThumbnail(page, i, currentPageIndex, onPageSelected, onPageReordered);
            filmstripThumbs.put(page.getId(), thumb);
            filmstripBox.getChildren().add(thumb);
        }
    }

    public void refreshFilmstripSelection(List<Page> pages, int currentPageIndex, Map<UUID, VBox> filmstripThumbs) {
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            VBox thumb = filmstripThumbs.get(page.getId());
            if (thumb != null) {
                applyThumbnailStyle(thumb, page, i == currentPageIndex);
            }
        }
    }

    // ===== Document tree nodes =====

    public void selectCurrentTreeItem(TreeView<DocumentTreeNode> treeView, Document selectedDocument, int selectedPageIndex) {
        if (selectedDocument == null) {
            return;
        }

        for (TreeItem<DocumentTreeNode> documentItem : treeView.getRoot().getChildren()) {
            if (!selectedDocument.getId().equals(documentItem.getValue().documentId())) {
                continue;
            }

            if (selectedPageIndex >= 0 && selectedPageIndex < documentItem.getChildren().size()) {
                treeView.getSelectionModel().select(documentItem.getChildren().get(selectedPageIndex));
            } else {
                treeView.getSelectionModel().select(documentItem);
            }
            return;
        }
    }

    private void setupDocumentTreeDragAndDrop(TreeView<DocumentTreeNode> treeView, PageMoveHandler onPageMoved) {
        treeView.setCellFactory(view -> {
            TreeCell<DocumentTreeNode> cell = new TreeCell<>() {
                @Override
                protected void updateItem(DocumentTreeNode item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().remove(DOCUMENT_DROP_TARGET);
                    setText(empty || item == null ? null : item.toString());
                }
            };

            cell.setOnDragDetected(event -> {
                DocumentTreeNode item = cell.getItem();
                if (item == null || !item.isPage()) {
                    return;
                }

                Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(TREE_PAGE_DRAG_PREFIX + item.documentId() + "|" + item.pageId() + "|" + item.pageIndex());
                dragboard.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                DocumentTreeNode target = cell.getItem();
                if (target != null && target.documentId() != null && hasTreePageDrag(event.getDragboard())) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    showDropTarget(cell);
                }
                event.consume();
            });

            cell.setOnDragExited(event -> {
                cell.getStyleClass().remove(DOCUMENT_DROP_TARGET);
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                boolean completed = false;
                DocumentTreeNode target = cell.getItem();
                cell.getStyleClass().remove(DOCUMENT_DROP_TARGET);

                if (target != null && target.documentId() != null && hasTreePageDrag(event.getDragboard())) {
                    String dragText = event.getDragboard().getString().substring(TREE_PAGE_DRAG_PREFIX.length());
                    String[] parts = dragText.split("\\|");
                    UUID sourceDocumentId = UUID.fromString(parts[0]);
                    UUID pageId = UUID.fromString(parts[1]);
                    int sourcePageIndex = Integer.parseInt(parts[2]);
                    int targetPageIndex = target.isPage() ? target.pageIndex() : -1;

                    completed = onPageMoved.movePage(
                            sourceDocumentId,
                            pageId,
                            sourcePageIndex,
                            target.documentId(),
                            targetPageIndex
                    );
                }

                event.setDropCompleted(completed);
                event.consume();
            });

            return cell;
        });
    }

    private void showDropTarget(TreeCell<DocumentTreeNode> cell) {
        if (!cell.getStyleClass().contains(DOCUMENT_DROP_TARGET)) {
            cell.getStyleClass().add(DOCUMENT_DROP_TARGET);
        }
    }

    private void fitTreeHeightToRows(TreeView<DocumentTreeNode> treeView, TreeItem<DocumentTreeNode> root) {
        int visibleRows = 0;
        for (TreeItem<DocumentTreeNode> documentItem : root.getChildren()) {
            visibleRows += countVisibleRows(documentItem);
        }
        treeView.setPrefHeight((visibleRows * DOCUMENT_TREE_ROW_HEIGHT) + DOCUMENT_TREE_ROW_HEIGHT);
    }

    private int countVisibleRows(TreeItem<DocumentTreeNode> item) {
        if (item == null) {
            return 0;
        }

        int rows = 1;
        if (!item.isExpanded()) {
            return rows;
        }

        for (TreeItem<DocumentTreeNode> child : item.getChildren()) {
            rows += countVisibleRows(child);
        }
        return rows;
    }

    private boolean hasTreePageDrag(Dragboard dragboard) {
        return dragboard.hasString() && dragboard.getString().startsWith(TREE_PAGE_DRAG_PREFIX);
    }

    // ===== Thumbnail nodes =====

    private VBox createThumbnail(
            Page page,
            int index,
            int currentPageIndex,
            IntConsumer onPageSelected,
            BiFunction<Integer, Integer, Boolean> onPageReordered
    ) {
        VBox thumb = new VBox(2);
        thumb.setAlignment(javafx.geometry.Pos.CENTER);
        applyThumbnailStyle(thumb, page, index == currentPageIndex);

        Label ref = new Label("REF-" + String.format("%03d", page.getReferenceScanOrder()));
        ref.getStyleClass().add(SMALL_TEXT);

        String indexText = page.isBarcodePage() ? "BARCODE" : "#" + page.getUiOrder();
        if (page.getRotation() != 0) {
            indexText += " " + page.getRotation() + "deg";
        }

        Label pageIndexLabel = new Label(indexText);
        pageIndexLabel.getStyleClass().add(SMALL_TEXT);

        thumb.getChildren().addAll(ref, pageIndexLabel);
        thumb.setOnMouseClicked(event -> onPageSelected.accept(index));
        setupThumbnailDragAndDrop(thumb, index, onPageReordered);
        return thumb;
    }

    private void setupThumbnailDragAndDrop(
            VBox thumb,
            int index,
            BiFunction<Integer, Integer, Boolean> onPageReordered
    ) {
        thumb.setOnDragDetected(event -> {
            Dragboard dragboard = thumb.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index));
            dragboard.setContent(content);
            event.consume();
        });

        thumb.setOnDragOver(event -> {
            if (event.getGestureSource() != thumb && hasFilmstripDrag(event.getDragboard())) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        thumb.setOnDragDropped(event -> {
            boolean completed = false;

            if (hasFilmstripDrag(event.getDragboard())) {
                int fromIndex = Integer.parseInt(event.getDragboard().getString());
                completed = onPageReordered.apply(fromIndex, index);
            }

            event.setDropCompleted(completed);
            event.consume();
        });
    }

    // ===== Thumbnail styling =====

    private void applyThumbnailStyle(VBox thumb, Page page, boolean selected) {
        thumb.getStyleClass().setAll(FILMSTRIP_THUMB);

        if (page.isBarcodePage()) {
            thumb.getStyleClass().add(FILMSTRIP_THUMB_BARCODE);
        }

        if (selected) {
            thumb.getStyleClass().add(FILMSTRIP_THUMB_ACTIVE);
        }
    }

    private boolean hasFilmstripDrag(Dragboard dragboard) {
        return dragboard.hasString() && dragboard.getString().matches("\\d+");
    }

    @FunctionalInterface
    public interface PageMoveHandler {
        boolean movePage(UUID sourceDocumentId, UUID pageId, int sourcePageIndex, UUID targetDocumentId, int targetPageIndex);
    }
}
