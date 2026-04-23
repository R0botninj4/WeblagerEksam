package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.BLL.BoxManager;
import com.eksam.weblagereksam.BLL.FxImageConverter;
import com.eksam.weblagereksam.BLL.ScanImportManager;
import com.eksam.weblagereksam.BLL.ScanWorkspaceManager;
import com.eksam.weblagereksam.BLL.ScanWorkspaceManager.BoxDataSnapshot;
import com.eksam.weblagereksam.GUI.Login.Session;
import javafx.fxml.FXML;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserScanningController {

    @FXML private Label labelProfile;
    @FXML private Label labelFilesCount;
    @FXML private Label labelDocsCount;
    @FXML private Label labelSessionTimer;
    @FXML private Label labelUser;
    @FXML private Label labelDocCount;
    @FXML private Label labelOutputName;
    @FXML private Label labelFormat;
    @FXML private Label labelPagePosition;
    @FXML private Label labelPageRef;
    @FXML private Label labelConnected;
    @FXML private Label labelRotationInfo;
    @FXML private Label labelStatusUser;
    @FXML private ComboBox<Box> comboBoxBoxes;

    @FXML private Button btnMultiPage;
    @FXML private Button btnSinglePage;
    @FXML private Button btnExport;
    @FXML private Button btnRotateCCW;
    @FXML private Button btnRotateCW;
    @FXML private Button btnDelete;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnSlideshow;
    @FXML private Button btnNavLeft;
    @FXML private Button btnNavRight;
    @FXML private Button btnFetchNext;
    @FXML private Button btnFetchTen;

    @FXML private VBox documentsContainer;
    @FXML private HBox filmstripBox;
    @FXML private ImageView pageImageView;

    private BoxManager boxManager;
    private ScanImportManager scanImportManager;
    private ScanWorkspaceManager scanWorkspaceManager;

    private Box currentBox;
    private Document selectedDocument;
    private final List<Document> currentDocuments = new ArrayList<>();
    private final List<Page> currentPages = new ArrayList<>();
    private final Map<UUID, List<Page>> pagesByDocument = new HashMap<>();
    private final Map<UUID, Image> pageImageCache = new HashMap<>();
    private final Map<UUID, VBox> filmstripThumbs = new HashMap<>();
    private int currentPageIndex = 0;
    private boolean importInProgress = false;
    private boolean loadingBoxData = false;
    private Stage progressPopup;

    @FXML
    public void initialize() {
        try {
            boxManager = new BoxManager();
            scanImportManager = new ScanImportManager();
            scanWorkspaceManager = new ScanWorkspaceManager();

            setupUserInfo();
            setupDefaultUi();
            setupButtons();
            setupKeyboardShortcuts();
            loadBoxesForCurrentUser();
            loadCurrentBoxDataAsync(false);
        } catch (Exception e) {
            showStatus("Scanner could not start.");
            e.printStackTrace();
        }
    }

    private void setupUserInfo() {
        String username = Session.getUser() != null ? Session.getUser().getUsername() : "Unknown";
        labelUser.setText(username);
        labelStatusUser.setText(username + " | Scanning");
    }

    private void setupDefaultUi() {
        labelFilesCount.setText("Files 0");
        labelDocsCount.setText("Docs 0");
        labelDocCount.setText("0 docs");
        labelSessionTimer.setText("00:00");
        labelFormat.setText("TIFF Multi-page");
        labelPagePosition.setText("0 / 0");
        labelPageRef.setText("No page selected");
        labelConnected.setText("Connected");
        labelRotationInfo.setText("Rotation: 0 degrees");
        btnExport.setText("Export (0 documents)");
        setupBoxDropdown();
    }

    private void setupBoxDropdown() {
        comboBoxBoxes.setConverter(new StringConverter<>() {
            @Override
            public String toString(Box box) {
                if (box == null) {
                    return "";
                }

                if (box.getLabel() == null || box.getLabel().isBlank()) {
                    return box.getBoxNumber();
                }

                return box.getBoxNumber() + " - " + box.getLabel();
            }

            @Override
            public Box fromString(String string) {
                return null;
            }
        });

        comboBoxBoxes.setOnAction(event -> {
            Box selectedBox = comboBoxBoxes.getSelectionModel().getSelectedItem();
            if (selectedBox == null || selectedBox.equals(currentBox)) {
                return;
            }

            currentBox = selectedBox;
            selectedDocument = null;
            currentPageIndex = 0;
            currentDocuments.clear();
            currentPages.clear();
            pagesByDocument.clear();
            filmstripThumbs.clear();
            updateBoxHeader();
            loadCurrentBoxDataAsync(false);
        });
    }

    private void setupButtons() {
        btnFetchNext.setOnAction(e -> handleFetchNext());
        btnFetchTen.setOnAction(e -> handleFetchTen());
        btnPrev.setOnAction(e -> showPreviousPage());
        btnNext.setOnAction(e -> showNextPage());
        btnNavLeft.setOnAction(e -> showPreviousPage());
        btnNavRight.setOnAction(e -> showNextPage());
        btnRotateCCW.setOnAction(e -> rotateCurrentPage(-90));
        btnRotateCW.setOnAction(e -> rotateCurrentPage(90));
        btnDelete.setOnAction(e -> deleteCurrentPage());
        btnMultiPage.setOnAction(e -> labelFormat.setText("TIFF Multi-page"));
        btnSinglePage.setOnAction(e -> labelFormat.setText("TIFF Single-page"));
        btnSlideshow.setOnAction(e -> showStatus("Slideshow is not ready yet."));
        btnExport.setOnAction(e -> showStatus("Export is not ready yet."));
    }

    private void setupKeyboardShortcuts() {
        pageImageView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            newScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.RIGHT) {
                    showNextPage();
                } else if (event.getCode() == KeyCode.LEFT) {
                    showPreviousPage();
                } else if (event.getCode() == KeyCode.DELETE) {
                    deleteCurrentPage();
                } else if (event.getCode() == KeyCode.R) {
                    if (event.isShiftDown()) {
                        rotateCurrentPage(-90);
                    } else {
                        rotateCurrentPage(90);
                    }
                }
            });
        });
    }

    private void loadBoxesForCurrentUser() throws Exception {
        List<Box> boxes;

        if (Session.getUser() != null) {
            boxes = boxManager.getBoxesByUserId(Session.getUser().getId());
        } else {
            boxes = boxManager.getAllBoxes();
        }

        comboBoxBoxes.getItems().setAll(boxes);

        if (!boxes.isEmpty()) {
            currentBox = boxes.get(0);
            comboBoxBoxes.getSelectionModel().select(currentBox);
        }

        if (currentBox == null) {
            labelProfile.setText("No Profile");
            labelOutputName.setText("No Box");
            comboBoxBoxes.setPromptText("No boxes");
            showStatus("No boxes found.");
            return;
        }

        updateBoxHeader();
    }

    private void updateBoxHeader() {
        labelProfile.setText(currentBox.getLabel() != null ? currentBox.getLabel() : "No profile");
        labelOutputName.setText(currentBox.getBoxNumber());
    }

    private void handleFetchNext() {
        startImportTask(1);
    }

    private void handleFetchTen() {
        startImportTask(10);
    }

    private void startImportTask(int amount) {
        if (importInProgress) {
            return;
        }

        if (currentBox == null) {
            showStatus("Select a box first.");
            return;
        }

        Task<Integer> importTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                if (amount == 1) {
                    return scanImportManager.importRandomTiffToBox(currentBox.getId());
                }
                return scanImportManager.importRandomTiffBatchToBox(currentBox.getId(), amount, (completed, total, message) -> {
                    updateProgress(completed, total);
                    updateMessage(message);
                });
            }
        };

        importTask.setOnRunning(event -> {
            importInProgress = true;
            btnFetchNext.setDisable(true);
            btnFetchTen.setDisable(true);
            showStatus("Fetching " + amount + " scans...");
            if (amount > 1) {
                showProgressPopup(importTask, amount);
            }
        });

        importTask.setOnSucceeded(event -> {
            importInProgress = false;
            btnFetchNext.setDisable(false);
            btnFetchTen.setDisable(false);
            closeProgressPopup();
            showStatus("Done. " + importTask.getValue() + " documents updated.");
            loadCurrentBoxDataAsync(false);
        });

        importTask.setOnFailed(event -> {
            importInProgress = false;
            btnFetchNext.setDisable(false);
            btnFetchTen.setDisable(false);
            closeProgressPopup();
            Throwable error = importTask.getException();
            showStatus("Scan failed.");
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread importThread = new Thread(importTask, "scan-import-thread");
        importThread.setDaemon(true);
        importThread.start();
    }

    private void showProgressPopup(Task<?> importTask, int amount) {
        closeProgressPopup();

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(280);
        progressBar.progressProperty().bind(importTask.progressProperty());

        Label titleLabel = new Label("Scanning " + amount + " files");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label statusLabel = new Label("Getting ready...");
        statusLabel.textProperty().bind(importTask.messageProperty());

        VBox content = new VBox(12, titleLabel, statusLabel, progressBar);
        content.setPadding(new Insets(16));
        content.setAlignment(Pos.CENTER_LEFT);
        content.setStyle("-fx-background-color: white;");

        progressPopup = new Stage();
        progressPopup.initModality(Modality.APPLICATION_MODAL);

        Window owner = pageImageView.getScene() != null ? pageImageView.getScene().getWindow() : null;
        if (owner != null) {
            progressPopup.initOwner(owner);
        }

        progressPopup.setTitle("Scanning");
        progressPopup.setResizable(false);
        progressPopup.setScene(new Scene(content));
        progressPopup.show();
    }

    private void closeProgressPopup() {
        if (progressPopup != null) {
            progressPopup.close();
            progressPopup = null;
        }
    }

    private void loadCurrentBoxDataAsync(boolean preserveStatusMessage) {
        if (loadingBoxData) {
            return;
        }

        Task<BoxDataSnapshot> loadTask = new Task<>() {
            @Override
            protected BoxDataSnapshot call() throws Exception {
                return fetchBoxDataSnapshot();
            }
        };

        loadTask.setOnRunning(event -> {
            loadingBoxData = true;
            setNavigationDisabled(true);
            if (!preserveStatusMessage) {
                showStatus("Loading pages...");
            }
        });

        loadTask.setOnSucceeded(event -> {
            loadingBoxData = false;
            setNavigationDisabled(false);
            applyBoxDataSnapshot(loadTask.getValue());
            if (!preserveStatusMessage) {
                showStatus("Ready");
            }
        });

        loadTask.setOnFailed(event -> {
            loadingBoxData = false;
            setNavigationDisabled(false);
            Throwable error = loadTask.getException();
            showStatus("Could not load pages.");
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread loadThread = new Thread(loadTask, "scan-box-load-thread");
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private BoxDataSnapshot fetchBoxDataSnapshot() {
        if (currentBox == null) {
            return new BoxDataSnapshot(List.of(), Map.of(), null, 0);
        }

        UUID selectedDocumentId = selectedDocument != null ? selectedDocument.getId() : null;
        return scanWorkspaceManager.loadBoxData(currentBox.getId(), selectedDocumentId);
    }

    private void applyBoxDataSnapshot(BoxDataSnapshot snapshot) {
        currentDocuments.clear();
        currentPages.clear();
        pagesByDocument.clear();
        filmstripThumbs.clear();

        currentDocuments.addAll(snapshot.documents());
        pagesByDocument.putAll(snapshot.pagesByDocument());

        labelDocsCount.setText("Docs " + currentDocuments.size());
        labelDocCount.setText(currentDocuments.size() + " docs");
        labelFilesCount.setText("Files " + snapshot.totalFiles());
        btnExport.setText("Export (" + currentDocuments.size() + " documents)");

        if (snapshot.selectedDocumentId() != null) {
            selectedDocument = currentDocuments.stream()
                    .filter(document -> document.getId().equals(snapshot.selectedDocumentId()))
                    .findFirst()
                    .orElse(null);
        }

        if (selectedDocument == null && !currentDocuments.isEmpty()) {
            selectedDocument = currentDocuments.get(0);
        }

        if (selectedDocument != null) {
            currentPages.addAll(copyPages(pagesByDocument.get(selectedDocument.getId())));
            if (currentPageIndex >= currentPages.size()) {
                currentPageIndex = Math.max(0, currentPages.size() - 1);
            }
        } else {
            currentPageIndex = 0;
        }

        renderDocumentCards();
        renderFilmstrip();
        showCurrentPage();
    }

    private void setNavigationDisabled(boolean disabled) {
        btnPrev.setDisable(disabled);
        btnNext.setDisable(disabled);
        btnNavLeft.setDisable(disabled);
        btnNavRight.setDisable(disabled);
        btnRotateCCW.setDisable(disabled);
        btnRotateCW.setDisable(disabled);
        btnDelete.setDisable(disabled);
    }

    private List<Page> copyPages(List<Page> pages) {
        return pages == null ? new ArrayList<>() : new ArrayList<>(pages);
    }

    private void renderDocumentCards() {
        documentsContainer.getChildren().clear();

        for (Document document : currentDocuments) {
            List<Page> pages = pagesByDocument.getOrDefault(document.getId(), List.of());
            VBox card = new VBox(4);
            card.setStyle(document.equals(selectedDocument)
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
            card.setOnMouseClicked(event -> selectDocument(document.getId(), 0));
            documentsContainer.getChildren().add(card);
        }
    }

    private void selectDocument(UUID documentId, int pageIndex) {
        selectedDocument = currentDocuments.stream()
                .filter(document -> document.getId().equals(documentId))
                .findFirst()
                .orElse(null);

        currentPages.clear();
        if (selectedDocument != null) {
            currentPages.addAll(copyPages(pagesByDocument.get(selectedDocument.getId())));
        }

        currentPageIndex = Math.max(0, Math.min(pageIndex, Math.max(0, currentPages.size() - 1)));
        renderDocumentCards();
        renderFilmstrip();
        showCurrentPage();
    }

    private void renderFilmstrip() {
        filmstripBox.getChildren().clear();
        filmstripThumbs.clear();

        if (selectedDocument == null) {
            Label empty = new Label("No pages");
            empty.setStyle("-fx-padding: 8;");
            filmstripBox.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < currentPages.size(); i++) {
            Page page = currentPages.get(i);
            VBox thumb = createThumbnail(page, i);
            filmstripThumbs.put(page.getId(), thumb);
            filmstripBox.getChildren().add(thumb);
        }
    }

    private VBox createThumbnail(Page page, int index) {
        VBox thumb = new VBox(2);
        thumb.setAlignment(javafx.geometry.Pos.CENTER);
        applyThumbnailStyle(thumb, page, index == currentPageIndex);

        ImageView preview = new ImageView(getCachedPageImage(page));
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
        thumb.setOnMouseClicked(event -> {
            currentPageIndex = index;
            refreshFilmstripSelection();
            showCurrentPage();
        });

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
                completed = reorderPage(fromIndex, index);
            }

            event.setDropCompleted(completed);
            event.consume();
        });

        return thumb;
    }

    private void applyThumbnailStyle(VBox thumb, Page page, boolean selected) {
        thumb.setStyle(selected
                ? (page.isBarcodePage()
                ? "-fx-padding: 4; -fx-border-color: #b4004e; -fx-border-width: 2; -fx-background-color: #ffd7e8;"
                : "-fx-padding: 4; -fx-border-color: #333333; -fx-border-width: 2; -fx-background-color: #dddddd;")
                : (page.isBarcodePage()
                ? "-fx-padding: 4; -fx-border-color: #d9719d; -fx-border-width: 1; -fx-background-color: #fff0f6;"
                : "-fx-padding: 4;"));
    }

    private void refreshFilmstripSelection() {
        for (int i = 0; i < currentPages.size(); i++) {
            Page page = currentPages.get(i);
            VBox thumb = filmstripThumbs.get(page.getId());
            if (thumb != null) {
                applyThumbnailStyle(thumb, page, i == currentPageIndex);
            }
        }
    }

    private Image getCachedPageImage(Page page) {
        return pageImageCache.computeIfAbsent(page.getId(), ignored -> FxImageConverter.bytesToFxImage(page.getImageData()));
    }

    private void showCurrentPage() {
        if (selectedDocument == null || currentPages.isEmpty()) {
            pageImageView.setImage(null);
            labelPagePosition.setText("0 / 0");
            labelPageRef.setText("No page selected");
            labelRotationInfo.setText("Rotation: 0 degrees");
            return;
        }

        Page page = currentPages.get(currentPageIndex);
        pageImageView.setImage(getCachedPageImage(page));
        labelPagePosition.setText((currentPageIndex + 1) + " / " + currentPages.size());
        labelPageRef.setText(
                "Document " + selectedDocument.getDocumentNumber()
                        + " | "
                        + page.getFileName()
                        + (page.isBarcodePage() ? " | BARCODE" : "")
        );
        labelRotationInfo.setText("Rotation: " + page.getRotation() + " degrees");
    }

    private void showNextPage() {
        if (currentPages.isEmpty()) {
            return;
        }

        if (currentPageIndex < currentPages.size() - 1) {
            currentPageIndex++;
            refreshFilmstripSelection();
            showCurrentPage();
        }
    }

    private void showPreviousPage() {
        if (currentPages.isEmpty()) {
            return;
        }

        if (currentPageIndex > 0) {
            currentPageIndex--;
            refreshFilmstripSelection();
            showCurrentPage();
        }
    }

    private void rotateCurrentPage(int delta) {
        if (selectedDocument == null || currentPages.isEmpty()) {
            return;
        }

        try {
            Page page = currentPages.get(currentPageIndex);
            if (scanWorkspaceManager.rotatePage(page, delta)) {
                pageImageCache.remove(page.getId());
                pagesByDocument.put(selectedDocument.getId(), copyPages(currentPages));
                renderFilmstrip();
                showCurrentPage();
                showStatus("Rotation saved.");
            } else {
                showStatus("Could not save rotation.");
            }
        } catch (Exception e) {
            showStatus("Rotation failed.");
            e.printStackTrace();
        }
    }

    private void deleteCurrentPage() {
        if (selectedDocument == null || currentPages.isEmpty()) {
            return;
        }

        try {
            Page page = currentPages.get(currentPageIndex);
            List<Page> remainingPages = new ArrayList<>(currentPages);
            remainingPages.remove(currentPageIndex);

            if (!scanWorkspaceManager.deletePage(page, selectedDocument.getId(), remainingPages)) {
                showStatus("Could not delete page.");
                return;
            }

            pageImageCache.remove(page.getId());
            filmstripThumbs.remove(page.getId());
            currentPages.clear();
            currentPages.addAll(remainingPages);

            if (currentPages.isEmpty()) {
                selectedDocument = null;
                currentPageIndex = 0;
            } else {
                if (currentPageIndex >= currentPages.size()) {
                    currentPageIndex = currentPages.size() - 1;
                }
            }

            loadCurrentBoxDataAsync(true);
            showStatus("Page deleted.");
        } catch (Exception e) {
            showStatus("Delete failed.");
            e.printStackTrace();
        }
    }

    private boolean reorderPage(int fromIndex, int toIndex) {
        if (selectedDocument == null || fromIndex == toIndex || fromIndex < 0 || toIndex < 0
                || fromIndex >= currentPages.size() || toIndex >= currentPages.size()) {
            return false;
        }

        Page movedPage = currentPages.remove(fromIndex);
        currentPages.add(toIndex, movedPage);

        try {
            if (!scanWorkspaceManager.updatePageOrders(selectedDocument.getId(), currentPages)) {
                throw new Exception("Database did not accept the new page order.");
            }
            currentPageIndex = toIndex;
            pagesByDocument.put(selectedDocument.getId(), copyPages(currentPages));
            renderDocumentCards();
            renderFilmstrip();
            showCurrentPage();
            showStatus("Page order saved.");
            return true;
        } catch (Exception e) {
            showStatus("Could not save page order.");
            e.printStackTrace();
            loadCurrentBoxDataAsync(true);
            return false;
        }
    }

    private void showStatus(String message) {
        labelConnected.setText(message);
    }
}
