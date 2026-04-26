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
import javafx.fxml.FXMLLoader;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserScanningController {

    // ===== FXML: Top bar and status labels =====

    @FXML private Label labelProfile, labelFilesCount, labelDocsCount, labelUser, labelDocCount;
    @FXML private Label labelOutputName, labelFormat, labelPagePosition, labelPageRef;
    @FXML private Label labelConnected, labelRotationInfo, labelStatusUser;
    @FXML private ComboBox<Box> comboBoxBoxes;

    // ===== FXML: Action buttons =====

    @FXML private Button btnExport, btnRotateCCW, btnRotateCW, btnDelete, btnPrev;
    @FXML private Button btnNext, btnNavLeft, btnNavRight, btnFetchNext, btnFetchTen;

    // ===== FXML: Main content containers =====

    @FXML private VBox documentsContainer;
    @FXML private HBox filmstripBox;
    @FXML private ImageView pageImageView;

    // ===== BLL / GUI helpers =====

    private BoxManager boxManager;
    private ScanImportManager scanImportManager;
    private ScanWorkspaceManager scanWorkspaceManager;
    private ScanViewRenderer scanViewRenderer;

    // ===== Current screen state =====

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

    // ===== Progress popup state =====

    private Stage progressPopup;
    private ScanProgressDialogController progressDialogController;

    // ===== JavaFX lifecycle =====

    @FXML
    public void initialize() {
        try {
            boxManager = new BoxManager();
            scanImportManager = new ScanImportManager();
            scanWorkspaceManager = new ScanWorkspaceManager();
            scanViewRenderer = new ScanViewRenderer();

            setupUserInfo();
            setupBoxDropdown();
            setupKeyboardShortcuts();
            loadBoxesForCurrentUser();
            loadCurrentBoxDataAsync(false);
        } catch (Exception e) {
            showStatus("Scanner could not start.");
            e.printStackTrace();
        }
    }

    // ===== Initial screen setup =====

    private void setupUserInfo() {
        String username = Session.getUser() != null ? Session.getUser().getUsername() : "Unknown";
        labelUser.setText(username);
        labelStatusUser.setText(username + " | Scanning");
    }

    private void setupBoxDropdown() {
        comboBoxBoxes.setConverter(new BoxDisplayConverter());
    }

    // ===== FXML actions: box and toolbar controls =====

    @FXML
    private void handleBoxSelected() {
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
    }

    private void setupKeyboardShortcuts() {
        pageImageView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            newScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.RIGHT) showNextPage();
                else if (event.getCode() == KeyCode.LEFT) showPreviousPage();
                else if (event.getCode() == KeyCode.DELETE) deleteCurrentPage();
                else if (event.getCode() == KeyCode.R) rotateCurrentPage(event.isShiftDown() ? -90 : 90);
            });
        });
    }

    private void loadBoxesForCurrentUser() throws Exception {
        List<Box> boxes = Session.getUser() != null
                ? boxManager.getBoxesByUserId(Session.getUser().getId())
                : boxManager.getAllBoxes();

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

    @FXML private void handleFetchNext() { startImportTask(1); }

    @FXML private void handleFetchTen() { startImportTask(10); }

    @FXML private void handleRotateLeft() { rotateCurrentPage(-90); }

    @FXML private void handleRotateRight() { rotateCurrentPage(90); }

    @FXML private void handleMultiPageFormat() { labelFormat.setText("TIFF Multi-page"); }

    @FXML private void handleSinglePageFormat() { labelFormat.setText("TIFF Single-page"); }

    @FXML private void handleSlideshow() { showStatus("Slideshow is not ready yet."); }

    @FXML private void handleExport() { showStatus("Export is not ready yet."); }

    // ===== Import / scanning flow =====

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

                updateProgress(0, amount);
                updateMessage("Fetching files from scanner...");
                return scanImportManager.importRandomTiffBatchToBox(currentBox.getId(), amount, (completed, total, message) -> {
                    updateProgress(completed, total);
                    updateMessage(message);
                });
            }
        };

        importTask.setOnRunning(event -> {
            importInProgress = true;
            setDisabled(true, btnFetchNext, btnFetchTen);
            showStatus("Fetching " + amount + " scans...");
            if (amount > 1) {
                showProgressPopup(importTask, amount);
            }
        });

        importTask.setOnSucceeded(event -> {
            finishImportTask();
            showStatus("Done. " + importTask.getValue() + " documents updated.");
            loadCurrentBoxDataAsync(false);
        });

        importTask.setOnFailed(event -> {
            finishImportTask();
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

    private void finishImportTask() {
        importInProgress = false;
        setDisabled(false, btnFetchNext, btnFetchTen);
        closeProgressPopup();
    }

    // ===== Progress popup =====

    private void showProgressPopup(Task<?> importTask, int amount) {
        closeProgressPopup();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eksam/weblagereksam/Scan-Progress-dialog.fxml"));
        VBox content;
        try {
            content = loader.load();
        } catch (IOException e) {
            showStatus("Could not open progress window.");
            e.printStackTrace();
            return;
        }

        progressDialogController = loader.getController();
        progressDialogController.bind(importTask, amount);

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
        if (progressDialogController != null) {
            progressDialogController.unbind();
            progressDialogController = null;
        }

        if (progressPopup != null) {
            progressPopup.close();
            progressPopup = null;
        }
    }

    // ===== Box data loading =====

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
        setDisabled(disabled, btnPrev, btnNext, btnNavLeft, btnNavRight, btnRotateCCW, btnRotateCW, btnDelete);
    }

    private void setDisabled(boolean disabled, Button... buttons) {
        for (Button button : buttons) {
            button.setDisable(disabled);
        }
    }

    private List<Page> copyPages(List<Page> pages) {
        return pages == null ? new ArrayList<>() : new ArrayList<>(pages);
    }

    // ===== Rendering helpers =====

    private void renderDocumentCards() {
        scanViewRenderer.renderDocumentCards(
                documentsContainer,
                currentDocuments,
                pagesByDocument,
                selectedDocument,
                documentId -> selectDocument(documentId, 0)
        );
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
        scanViewRenderer.renderFilmstrip(
                filmstripBox,
                currentPages,
                selectedDocument != null,
                currentPageIndex,
                this::getCachedPageImage,
                index -> {
                    currentPageIndex = index;
                    refreshFilmstripSelection();
                    showCurrentPage();
                },
                this::reorderPage,
                filmstripThumbs
        );
    }

    private void refreshFilmstripSelection() {
        scanViewRenderer.refreshFilmstripSelection(currentPages, currentPageIndex, filmstripThumbs);
    }

    private Image getCachedPageImage(Page page) {
        return pageImageCache.computeIfAbsent(page.getId(), ignored -> FxImageConverter.bytesToFxImage(page.getImageData()));
    }

    // ===== Page viewer =====

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

    // ===== Page navigation =====

    @FXML private void showNextPage() { movePage(1); }

    @FXML private void showPreviousPage() { movePage(-1); }

    private void movePage(int direction) {
        if (currentPages.isEmpty()) {
            return;
        }

        int nextIndex = currentPageIndex + direction;
        if (nextIndex >= 0 && nextIndex < currentPages.size()) {
            currentPageIndex = nextIndex;
            refreshFilmstripSelection();
            showCurrentPage();
        }
    }

    // ===== Page actions =====

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

    @FXML
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
            } else if (currentPageIndex >= currentPages.size()) {
                currentPageIndex = currentPages.size() - 1;
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

    // ===== Status message =====

    private void showStatus(String message) {
        labelConnected.setText(message);
    }
}
