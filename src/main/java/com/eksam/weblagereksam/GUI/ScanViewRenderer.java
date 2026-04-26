package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.function.Function;
import java.util.function.IntConsumer;

public class ScanViewRenderer {

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
            Function<Page, Image> imageLoader,
            IntConsumer onPageSelected,
            BiFunction<Integer, Integer, Boolean> onPageReordered,
            Map<UUID, VBox> filmstripThumbs
    ) {
        filmstripBox.getChildren().clear();
        filmstripThumbs.clear();

        if (!hasSelectedDocument) {
            Label empty = new Label("No pages");
            empty.setStyle("-fx-padding: 8;");
            filmstripBox.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            VBox thumb = createThumbnail(page, i, currentPageIndex, imageLoader, onPageSelected, onPageReordered);
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
        card.setStyle(selected
                ? "-fx-border-color: #333333; -fx-border-width: 2; -fx-padding: 8; -fx-background-color: #f1f1f1;"
                : "-fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-padding: 8;");

        HBox header = new HBox(8);
        Label title = new Label("Document " + document.getDocumentNumber());
        title.setStyle("-fx-font-weight: bold;");
        Label fileCount = new Label(pages.size() + " pages");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, fileCount);

        HBox pageMarkers = new HBox(4);
        for (int i = 0; i < pages.size(); i++) {
            String markerText = pages.get(i).isBarcodePage() ? "[B]" : "[" + (i + 1) + "]";
            Label marker = new Label(markerText);
            marker.setStyle("-fx-font-size: 10;");
            pageMarkers.getChildren().add(marker);
        }

        if (document.getBarcodeValue() != null && !document.getBarcodeValue().isBlank()) {
            Label barcode = new Label("Split: " + document.getBarcodeValue());
            barcode.setStyle("-fx-font-size: 10;");
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
            Function<Page, Image> imageLoader,
            IntConsumer onPageSelected,
            BiFunction<Integer, Integer, Boolean> onPageReordered
    ) {
        VBox thumb = new VBox(2);
        thumb.setAlignment(javafx.geometry.Pos.CENTER);
        applyThumbnailStyle(thumb, page, index == currentPageIndex);

        ImageView preview = new ImageView(imageLoader.apply(page));
        preview.setFitWidth(50);
        preview.setFitHeight(66);
        preview.setPreserveRatio(true);

        Label ref = new Label("REF-" + String.format("%03d", page.getReferenceScanOrder()));
        ref.setStyle("-fx-font-size: 9;");

        String indexText = page.isBarcodePage() ? "BARCODE" : "#" + page.getUiOrder();
        if (page.getRotation() != 0) {
            indexText += " " + page.getRotation() + "deg";
        }

        Label pageIndexLabel = new Label(indexText);
        pageIndexLabel.setStyle("-fx-font-size: 9;");

        thumb.getChildren().addAll(preview, ref, pageIndexLabel);
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
        thumb.setStyle(selected
                ? selectedThumbnailStyle(page)
                : defaultThumbnailStyle(page));
    }

    private String selectedThumbnailStyle(Page page) {
        if (page.isBarcodePage()) {
            return "-fx-padding: 4; -fx-border-color: #b4004e; -fx-border-width: 2; -fx-background-color: #ffd7e8;";
        }

        return "-fx-padding: 4; -fx-border-color: #333333; -fx-border-width: 2; -fx-background-color: #dddddd;";
    }

    private String defaultThumbnailStyle(Page page) {
        if (page.isBarcodePage()) {
            return "-fx-padding: 4; -fx-border-color: #d9719d; -fx-border-width: 1; -fx-background-color: #fff0f6;";
        }

        return "-fx-padding: 4;";
    }
}
