package com.eksam.weblagereksam.GUI.Renderer;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ScanViewRenderer {

    private static final String DOCUMENT_CARD = "document-card";
    private static final String DOCUMENT_CARD_ACTIVE = "document-card-active";
    private static final String FILMSTRIP_THUMB = "filmstrip-thumb";
    private static final String FILMSTRIP_THUMB_ACTIVE = "filmstrip-thumb-active";
    private static final String FILMSTRIP_THUMB_BARCODE = "filmstrip-thumb-barcode";
    private static final String SMALL_TEXT = "small-text";

    // ===== Document list =====

    public void renderDocumentCards(
            VBox documentsContainer,
            List<Document> documents,
            Map<UUID, List<Page>> pagesByDocument,
            Document selectedDocument,
            Consumer<UUID> onDocumentSelected
    ) {
        documentsContainer.getChildren().clear();

        for (Document document : documents) {
            documentsContainer.getChildren().add(createDocumentCard(
                    document,
                    pagesByDocument.getOrDefault(document.getId(), List.of()),
                    document.equals(selectedDocument),
                    onDocumentSelected
            ));
        }
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

    // ===== Document card nodes =====

    private VBox createDocumentCard(
            Document document,
            List<Page> pages,
            boolean selected,
            Consumer<UUID> onDocumentSelected
    ) {
        VBox card = new VBox(4);
        card.getStyleClass().add(DOCUMENT_CARD);

        if (selected) {
            card.getStyleClass().add(DOCUMENT_CARD_ACTIVE);
        }

        HBox header = new HBox(8);
        Label title = new Label("Document " + document.getDocumentNumber());
        title.getStyleClass().add("h3");
        Label fileCount = new Label(pages.size() + " pages");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, fileCount);

        HBox pageMarkers = new HBox(4);
        for (int i = 0; i < pages.size(); i++) {
            String markerText = pages.get(i).isBarcodePage() ? "[B]" : "[" + (i + 1) + "]";
            Label marker = new Label(markerText);
            marker.getStyleClass().add(SMALL_TEXT);
            pageMarkers.getChildren().add(marker);
        }

        if (document.getBarcodeValue() != null && !document.getBarcodeValue().isBlank()) {
            Label barcode = new Label("Split: " + document.getBarcodeValue());
            barcode.getStyleClass().add(SMALL_TEXT);
            pageMarkers.getChildren().add(barcode);
        }

        Label status = new Label(document.getStatus());
        card.getChildren().addAll(header, pageMarkers, status);
        card.setOnMouseClicked(event -> onDocumentSelected.accept(document.getId()));
        return card;
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
