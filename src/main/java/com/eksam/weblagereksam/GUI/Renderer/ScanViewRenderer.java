package com.eksam.weblagereksam.GUI.Renderer;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import javafx.scene.control.Label;
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

    public record DocumentTreeNode(UUID documentId, int pageIndex, String text) {
        public static DocumentTreeNode root() {
            return new DocumentTreeNode(null, -1, "Documents");
        }

        public static DocumentTreeNode document(Document document, List<Page> pages) {
            return new DocumentTreeNode(
                    document.getId(),
                    -1,
                    "Document " + document.getDocumentNumber() + " (" + pages.size() + " pages)"
            );
        }

        public static DocumentTreeNode page(Document document, Page page, int pageIndex) {
            String text = page.isBarcodePage() ? "Barcode page" : "Page " + page.getUiOrder();

            if (page.getRotation() != 0) {
                text += " - " + page.getRotation() + " degrees";
            }

            return new DocumentTreeNode(document.getId(), pageIndex, text);
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
            int selectedPageIndex
    ) {
        TreeItem<DocumentTreeNode> root = new TreeItem<>(DocumentTreeNode.root());
        root.setExpanded(true);

        for (Document document : documents) {
            List<Page> pages = pagesByDocument.getOrDefault(document.getId(), List.of());
            TreeItem<DocumentTreeNode> documentItem = new TreeItem<>(DocumentTreeNode.document(document, pages));
            documentItem.setExpanded(document.equals(selectedDocument));

            for (int i = 0; i < pages.size(); i++) {
                documentItem.getChildren().add(new TreeItem<>(DocumentTreeNode.page(document, pages.get(i), i)));
            }

            root.getChildren().add(documentItem);
        }

        documentTreeView.setRoot(root);
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

    private void selectCurrentTreeItem(TreeView<DocumentTreeNode> treeView, Document selectedDocument, int selectedPageIndex) {
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
            if (event.getGestureSource() != thumb && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        thumb.setOnDragDropped(event -> {
            boolean completed = false;

            if (event.getDragboard().hasString()) {
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
}
