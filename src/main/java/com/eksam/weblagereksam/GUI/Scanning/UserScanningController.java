package com.eksam.weblagereksam.GUI.Scanning;
import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.BLL.Image.FxImageConverter;
import com.eksam.weblagereksam.BLL.Manager.BoxManager;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import com.eksam.weblagereksam.BLL.Manager.ExportManager.ExportFormat;
import com.eksam.weblagereksam.BLL.Manager.LogManager;
import com.eksam.weblagereksam.BLL.Manager.ProfileManager;
import com.eksam.weblagereksam.BLL.Manager.ScanImportManager;
import com.eksam.weblagereksam.BLL.Manager.ScanWorkspaceManager;
import com.eksam.weblagereksam.BLL.Manager.ScanWorkspaceManager.BoxDataSnapshot;
import com.eksam.weblagereksam.GUI.Login.Session;
import com.eksam.weblagereksam.GUI.Renderer.ScanViewRenderer;
import com.eksam.weblagereksam.GUI.Renderer.ScanViewRenderer.DocumentTreeNode;
import com.eksam.weblagereksam.GUI.Util.ErrorDialog;
import com.eksam.weblagereksam.GUI.Util.LogoutHelper;
import com.eksam.weblagereksam.GUI.Util.ThemeSwitcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
public class UserScanningController {

    // ===== FXML fields =====

    @FXML private Label labelClient, labelProfile, labelFilesCount, labelDocsCount, labelUser, labelDocCount;
    @FXML private Label labelPagePosition, labelPageRef;
    @FXML private Label labelConnected, labelRotationInfo, labelStatusUser;
    @FXML private ComboBox<Integer> comboRotationDegrees;
    @FXML private BorderPane scanRoot;
    @FXML private Button btnRotateCCW, btnRotateCW, btnDeletePage, btnPrev;
    @FXML private Button btnNext, btnNavLeft, btnNavRight, btnFetchNext, btnFetchTen;
    @FXML private Button btnThemeToggle, btnStartScan, btnMyBoxes;
    @FXML private TreeView<DocumentTreeNode> documentTreeView;
    @FXML private HBox filmstripBox;
    @FXML private StackPane imageViewerPane;
    @FXML private ImageView pageImageView;

    // ===== Managers and helpers =====

    private BoxManager boxManager;
    private ClientManager clientManager;
    private ProfileManager profileManager;
    private ScanImportManager scanImportManager;
    private ScanWorkspaceManager scanWorkspaceManager;
    private LogManager logManager;
    private ScanViewRenderer scanViewRenderer;
    private ThemeSwitcher themeSwitcher;
    private LogoutHelper logoutHelper;
    private ScanProgressPopup scanProgressPopup;
    private ScanExportHelper scanExportHelper;

    // ===== Current scan state =====

    private Box currentBox;
    private Document selectedDocument;
    private final List<Client> availableClients = new ArrayList<>();
    private final List<Profile> availableProfiles = new ArrayList<>();
    private final List<Box> savedBoxes = new ArrayList<>();
    private final List<Document> currentDocuments = new ArrayList<>();
    private final List<Page> currentPages = new ArrayList<>();
    private final Map<UUID, List<Page>> pagesByDocument = new HashMap<>();
    private final Map<UUID, Image> pageImageCache = new HashMap<>();
    private final Map<UUID, VBox> filmstripThumbs = new HashMap<>();
    private final Set<UUID> loadingPageIds = new HashSet<>();
    private int currentPageIndex = 0;
    private boolean importInProgress = false;
    private boolean loadingBoxData = false;
    private boolean updatingDocumentTree = false;
    private boolean updatingRotationChoice = false;
    private ExportFormat selectedExportFormat = ExportFormat.MULTI_PAGE;

    // ===== Startup =====

    @FXML
    public void initialize() {
        try {
            boxManager = new BoxManager();
            clientManager = new ClientManager();
            profileManager = new ProfileManager();
            scanImportManager = new ScanImportManager();
            scanWorkspaceManager = new ScanWorkspaceManager();
            logManager = new LogManager();
            scanViewRenderer = new ScanViewRenderer();
            themeSwitcher = new ThemeSwitcher();
            logoutHelper = new LogoutHelper();
            scanProgressPopup = new ScanProgressPopup();
            scanExportHelper = new ScanExportHelper();
            setupUserInfo();
            setupKeyboardShortcuts();
            setupDocumentTree();
            setupImageViewer();
            setupRotationChoices();
            showNoBoxSelected();
            loadStartupDataAsync();
        } catch (Exception e) {
            showStatus("Scanner could not start.");
            showError("Scanner could not start.", e);
        }
    }
    private void setupUserInfo() {
        String username = Session.getUser() != null ? Session.getUser().getUsername() : "Unknown";
        labelUser.setText(username);
        labelStatusUser.setText(username + " | Scanning");
    }

    // ===== Box selection =====

    private void openBoxAsync(String boxNumber, Profile selectedProfile) {
        // Opening a box can touch the database, so it runs in a JavaFX Task.
        // That keeps the UI from freezing while the box is created or loaded.
        Task<Box> openTask = new Task<>() {
            @Override
            protected Box call() throws Exception {
                return boxManager.openBoxForScanning(boxNumber, selectedProfile, getCurrentUserId());
            }
        };
        openTask.setOnRunning(event -> {
            setDisabled(true, btnStartScan, btnMyBoxes);
            showStatus("Opening box...");
        });
        openTask.setOnSucceeded(event -> {
            setDisabled(false, btnStartScan, btnMyBoxes);
            Box box = openTask.getValue();
            loadSavedBoxesForCurrentUserAsync(box);
            openBox(box);
        });
        openTask.setOnFailed(event -> {
            setDisabled(false, btnStartScan, btnMyBoxes);
            showStatus("Could not open box.");
            showTaskError("Could not open box.", openTask);
        });
        runInBackground(openTask, "scan-open-box-thread");
    }
    private void openBox(Box box) {
        // This method changes the current workspace to the selected box.
        // After this, documents and pages are loaded for that box.
        currentBox = box;
        selectedDocument = null;
        currentPageIndex = 0;
        currentDocuments.clear();
        currentPages.clear();
        pagesByDocument.clear();
        filmstripThumbs.clear();
        writeLog("Open box", "Boxes", box.getId(), null, box.getBoxNumber());
        updateBoxHeader();
        loadCurrentBoxDataAsync(false);
    }
    private void setupRotationChoices() {
        comboRotationDegrees.getItems().setAll(0, 90, 180, 270);
        comboRotationDegrees.getSelectionModel().select(Integer.valueOf(0));
        comboRotationDegrees.getEditor().setOnAction(event -> handleRotationSelected());
        comboRotationDegrees.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                handleRotationSelected();
            }
        });
    }
    private void setupImageViewer() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageViewerPane.widthProperty());
        clip.heightProperty().bind(imageViewerPane.heightProperty());
        imageViewerPane.setClip(clip);
        // The rotated image is only visual. It should never block clicks on toolbar buttons.
        pageImageView.setMouseTransparent(true);
    }

    private void setupDocumentTree() {
        documentTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (updatingDocumentTree || newItem == null || newItem.getValue().documentId() == null) {
                return;
            }

            DocumentTreeNode node = newItem.getValue();
            selectDocument(node.documentId(), Math.max(0, node.pageIndex()));
        });
    }

    // ===== Loading saved boxes and startup data =====

    private void loadSavedBoxesForCurrentUserAsync(Box boxToSelect) {
        Task<List<Box>> loadTask = new Task<>() {
            @Override
            protected List<Box> call() {
                UUID userId = getCurrentUserId();
                return userId != null ? boxManager.getBoxesByUserId(userId) : List.of();
            }
        };
        loadTask.setOnSucceeded(event -> {
            savedBoxes.clear();
            savedBoxes.addAll(loadTask.getValue());
        });
        loadTask.setOnFailed(event -> showTaskError("Could not load saved boxes.", loadTask));
        runInBackground(loadTask, "scan-saved-boxes-load-thread");
    }
    private void loadStartupDataAsync() {
        Task<StartupData> startupTask = new Task<>() {
            @Override
            protected StartupData call() {
                UUID userId = getCurrentUserId();
                List<Box> savedBoxes = userId != null ? boxManager.getBoxesByUserId(userId) : List.of();
                return new StartupData(clientManager.getActiveClients(), profileManager.getActiveProfiles(), savedBoxes);
            }
        };
        startupTask.setOnRunning(event -> showStatus("Loading scanner data..."));
        startupTask.setOnSucceeded(event -> {
            StartupData data = startupTask.getValue();
            availableClients.clear();
            availableClients.addAll(data.clients());
            availableProfiles.clear();
            availableProfiles.addAll(data.profiles());
            savedBoxes.clear();
            savedBoxes.addAll(data.savedBoxes());
            showStatus("Press Start scan to open a box.");
        });
        startupTask.setOnFailed(event -> {
            showStatus("Could not load scanner data.");
            showTaskError("Could not load scanner data.", startupTask);
        });
        runInBackground(startupTask, "scan-startup-load-thread");
    }
    private UUID getCurrentUserId() {
        return Session.getUser() != null ? Session.getUser().getId() : null;
    }
    // ===== Keyboard and header UI =====

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
    private void updateBoxHeader() {
        labelClient.setText("Client: " + (currentBox.getClientName() != null ? currentBox.getClientName() : "No client"));
        labelProfile.setText("Profile: " + (currentBox.getProfileName() != null ? currentBox.getProfileName() : "No profile"));
    }
    private void showNoBoxSelected() {
        labelClient.setText("Client: No client");
        labelProfile.setText("Profile: No profile");
        showStatus("Press Start scan to open a box.");
    }

    // ===== Button actions =====

    @FXML private void handleStartScan() { openStartScanPopup(); }
    @FXML private void handleMyBoxes() { openMyBoxesPopup(); }
    @FXML private void handleFetchNext() { startImportTask(1); }
    @FXML private void handleFetchTen() { startImportTask(10); }
    @FXML private void handleRotateLeft() { rotateCurrentPage(-90); }
    @FXML private void handleRotateRight() { rotateCurrentPage(90); }
    @FXML
    private void handleRotationSelected() {
        if (updatingRotationChoice) {
            return;
        }
        Integer selectedRotation = parseRotationInput();
        if (selectedRotation != null) {
            setCurrentPageRotation(selectedRotation);
        } else if (!currentPages.isEmpty()) {
            setRotationChoice(currentPages.get(currentPageIndex).getRotation());
            showStatus("Enter a number between 0 and 359.");
        }
    }
    private void openSavedBox(Box selectedBox) {
        if (selectedBox == null) {
            showStatus("Select a saved box first.");
            return;
        }
        openBox(selectedBox);
    }
    private void handleCompleteCurrentBox() {
        if (currentBox == null) {
            showStatus("Open a box first.");
            return;
        }
        currentBox.setStatus("COMPLETED");
        completeBoxAsync(currentBox);
    }
    private void completeBoxAsync(Box box) {
        // Completing a box updates its status and removes it from the user's active list.
        // It runs in the background because it writes to the database.
        Task<Boolean> completeTask = new Task<>() {
            @Override
            protected Boolean call() {
                if (!boxManager.updateBox(box)) {
                    return false;
                }
                removeBoxFromCurrentUser(box);
                return true;
            }
        };
        completeTask.setOnRunning(event -> {
            setDisabled(true, btnMyBoxes);
            showStatus("Marking box as done...");
        });
        completeTask.setOnSucceeded(event -> {
            setDisabled(false, btnMyBoxes);
            if (!completeTask.getValue()) {
                showStatus("Could not mark box as done.");
                return;
            }
            writeLog("Complete box", "Boxes", box.getId(), null, box.getBoxNumber());
            clearCurrentBox();
            loadSavedBoxesForCurrentUserAsync(null);
            showStatus("Box marked as done.");
        });
        completeTask.setOnFailed(event -> {
            setDisabled(false, btnMyBoxes);
            showStatus("Could not mark box as done.");
            showTaskError("Could not mark box as done.", completeTask);
        });
        runInBackground(completeTask, "scan-complete-box-thread");
    }
    private void handleRemoveCurrentBox(Box selectedSavedBox) {
        Box boxToRemove = selectedSavedBox != null ? selectedSavedBox : currentBox;
        if (boxToRemove == null) {
            showStatus("Select a saved box first.");
            return;
        }
        removeBoxAsync(boxToRemove);
    }
    private void removeBoxAsync(Box boxToRemove) {
        // Remove only disconnects the box from this user.
        // It does not delete the box or scanned pages from the database.
        Task<Void> removeTask = new Task<>() {
            @Override
            protected Void call() {
                removeBoxFromCurrentUser(boxToRemove);
                return null;
            }
        };
        removeTask.setOnRunning(event -> {
            setDisabled(true, btnMyBoxes);
            showStatus("Removing box from your list...");
        });
        removeTask.setOnSucceeded(event -> {
            setDisabled(false, btnMyBoxes);
            if (currentBox != null && currentBox.getId().equals(boxToRemove.getId())) {
                clearCurrentBox();
            }
            writeLog("Remove box from user", "Boxes", boxToRemove.getId(), null, boxToRemove.getBoxNumber());
            loadSavedBoxesForCurrentUserAsync(currentBox);
            showStatus("Box removed from your list.");
        });
        removeTask.setOnFailed(event -> {
            setDisabled(false, btnMyBoxes);
            showStatus("Could not remove box.");
            showTaskError("Could not remove box.", removeTask);
        });
        runInBackground(removeTask, "scan-remove-box-thread");
    }
    @FXML
    private void handleThemeToggle() {
        themeSwitcher.toggleTheme(scanRoot, btnThemeToggle);
        showStatus(themeSwitcher.isDarkMode() ? "Dark mode enabled." : "Light mode enabled.");
    }
    @FXML
    private void handleLogout() {
        logoutHelper.logout(scanRoot.getScene().getWindow());
    }

    private void removeBoxFromCurrentUser(Box box) {
        UUID userId = getCurrentUserId();
        if (userId != null && box != null) {
            boxManager.removeBoxFromUser(userId, box.getId());
        }
    }
    private void clearCurrentBox() {
        currentBox = null;
        selectedDocument = null;
        currentPageIndex = 0;
        currentDocuments.clear();
        currentPages.clear();
        pagesByDocument.clear();
        filmstripThumbs.clear();
        pageImageCache.clear();
        labelDocsCount.setText("Docs 0");
        labelDocCount.setText("0 docs");
        labelFilesCount.setText("Files 0");
        renderDocumentCards();
        renderFilmstrip();
        showCurrentPage();
        showNoBoxSelected();
    }

    // ===== Import scanning =====

    private void startImportTask(int amount) {
        // This starts the actual scan/import work.
        // amount 1 fetches one random TIFF, amount 10 fetches a batch.
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
            writeLog("Scan files", "Boxes", currentBox.getId(), null, importTask.getValue() + " files");
            loadCurrentBoxDataAsync(false);
        });
        importTask.setOnFailed(event -> {
            finishImportTask();
            showStatus("Scan failed.");
            showTaskError("Scan failed.", importTask);
        });
        runInBackground(importTask, "scan-import-thread");
    }
    private void finishImportTask() {
        importInProgress = false;
        setDisabled(false, btnFetchNext, btnFetchTen);
        scanProgressPopup.close();
    }
    private void showProgressPopup(Task<?> importTask, int amount) {
        Window owner = pageImageView.getScene() != null ? pageImageView.getScene().getWindow() : null;
        if (!scanProgressPopup.show(importTask, amount, owner)) {
            showStatus("Could not open progress window.");
        }
    }

    // ===== Load documents and pages =====

    private void loadCurrentBoxDataAsync(boolean preserveStatusMessage) {
        // Loading box data can be slow because pages contain image data.
        // Therefore it runs in the background and updates the UI when done.
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
            showStatus("Could not load pages.");
            showTaskError("Could not load pages.", loadTask);
        });
        runInBackground(loadTask, "scan-box-load-thread");
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
        setDisabled(disabled, btnPrev, btnNext, btnNavLeft, btnNavRight, btnRotateCCW, btnRotateCW, btnDeletePage);
    }
    private void setDisabled(boolean disabled, Button... buttons) {
        for (Button button : buttons) {
            button.setDisable(disabled);
        }
    }
    private List<Page> copyPages(List<Page> pages) {
        return pages == null ? new ArrayList<>() : new ArrayList<>(pages);
    }

    // ===== Render document list and filmstrip =====

    private void renderDocumentCards() {
        updatingDocumentTree = true;
        scanViewRenderer.renderDocumentTree(
                documentTreeView,
                currentDocuments,
                pagesByDocument,
                selectedDocument,
                currentPageIndex,
                this::movePageInDocumentTree
        );
        updatingDocumentTree = false;
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
    private void showCurrentPage() {
        // Shows the selected page in the big preview area.
        // If the image has not been loaded yet, it starts loading in the background.
        if (selectedDocument == null || currentPages.isEmpty()) {
            pageImageView.setImage(null);
            pageImageView.setRotate(0);
            labelPagePosition.setText("0 / 0");
            labelPageRef.setText("No page selected");
            labelRotationInfo.setText("Rotation: 0 degrees");
            setRotationChoice(0);
            return;
        }
        Page page = currentPages.get(currentPageIndex);
        labelPagePosition.setText((currentPageIndex + 1) + " / " + currentPages.size());
        labelPageRef.setText(
                "Document " + selectedDocument.getDocumentNumber()
                        + " | "
                        + page.getFileName()
                        + (page.isBarcodePage() ? " | BARCODE" : "")
        );
        labelRotationInfo.setText("Rotation: " + page.getRotation() + " degrees");
        pageImageView.setRotate(page.getRotation());
        setRotationChoice(page.getRotation());
        Image cachedImage = pageImageCache.get(page.getId());
        if (cachedImage != null) {
            pageImageView.setImage(cachedImage);
            return;
        }
        if (page.getImageData() == null) {
            pageImageView.setImage(null);
            showStatus("Loading page...");
            loadPageImageAsync(page.getId());
            return;
        }
        pageImageView.setImage(getCachedPageImage(page));
    }

    // ===== Lazy page image loading =====

    private void loadPageImageAsync(UUID pageId) {
        if (loadingPageIds.contains(pageId)) {
            return;
        }
        loadingPageIds.add(pageId);
        Task<PageImageLoad> loadTask = new Task<>() {
            @Override
            protected PageImageLoad call() {
                Page loadedPage = scanWorkspaceManager.loadPage(pageId);
                Image image = loadedPage != null ? FxImageConverter.bytesToFxImage(loadedPage.getImageData()) : null;
                return new PageImageLoad(loadedPage, image);
            }
        };
        loadTask.setOnSucceeded(event -> {
            loadingPageIds.remove(pageId);
            PageImageLoad pageLoad = loadTask.getValue();
            if (pageLoad.page() == null) {
                showStatus("Could not load page.");
                return;
            }
            replacePage(pageLoad.page());
            if (pageLoad.image() != null) {
                pageImageCache.put(pageId, pageLoad.image());
            }
            if (!currentPages.isEmpty() && currentPages.get(currentPageIndex).getId().equals(pageId)) {
                pageImageView.setImage(pageLoad.image());
                pageImageView.setRotate(pageLoad.page().getRotation());
                showStatus("Ready");
            }
        });
        loadTask.setOnFailed(event -> {
            loadingPageIds.remove(pageId);
            showStatus("Could not load page.");
            showTaskError("Could not load page.", loadTask);
        });
        runInBackground(loadTask, "scan-page-load-thread");
    }
    private void replacePage(Page loadedPage) {
        replacePageInList(currentPages, loadedPage);
        List<Page> documentPages = pagesByDocument.get(loadedPage.getDocumentId());
        if (documentPages != null) {
            replacePageInList(documentPages, loadedPage);
        }
    }
    private void replacePageInList(List<Page> pages, Page loadedPage) {
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getId().equals(loadedPage.getId())) {
                pages.set(i, loadedPage);
                return;
            }
        }
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

    // ===== Rotation =====

    private void rotateCurrentPage(int delta) {
        if (selectedDocument == null || currentPages.isEmpty()) {
            return;
        }
        Page page = currentPages.get(currentPageIndex);
        setCurrentPageRotation(page.getRotation() + delta);
    }
    private void setCurrentPageRotation(int rotation) {
        if (selectedDocument == null || currentPages.isEmpty()) {
            return;
        }
        Page page = currentPages.get(currentPageIndex);
        int oldRotation = page.getRotation();
        int newRotation = normalizeRotation(rotation);
        if (oldRotation == newRotation) {
            return;
        }
        page.setRotation(newRotation);
        pagesByDocument.put(selectedDocument.getId(), copyPages(currentPages));
        pageImageView.setRotate(newRotation);
        labelRotationInfo.setText("Rotation: " + newRotation + " degrees");
        setRotationChoice(newRotation);
        renderFilmstrip();
        showStatus("Rotation saved.");
        saveRotationInBackground(page, oldRotation, newRotation);
    }
    private void saveRotationInBackground(Page page, int oldRotation, int newRotation) {
        // The UI rotation happens immediately.
        // The database save happens in the background so buttons do not feel stuck.
        Task<Boolean> rotationTask = new Task<>() {
            @Override
            protected Boolean call() {
                return scanWorkspaceManager.setPageRotation(page, newRotation);
            }
        };
        rotationTask.setOnFailed(event -> {
            rollbackRotation(page, oldRotation, newRotation);
            showTaskError("Could not save rotation.", rotationTask);
        });
        rotationTask.setOnSucceeded(event -> {
            if (!rotationTask.getValue()) {
                rollbackRotation(page, oldRotation, newRotation);
            }
        });
        runInBackground(rotationTask, "scan-rotation-save-thread");
    }
    private void rollbackRotation(Page page, int oldRotation, int failedRotation) {
        if (page.getRotation() != failedRotation) {
            return;
        }
        page.setRotation(oldRotation);
        if (!currentPages.isEmpty() && currentPages.get(currentPageIndex).getId().equals(page.getId())) {
            pageImageView.setRotate(oldRotation);
            labelRotationInfo.setText("Rotation: " + oldRotation + " degrees");
            setRotationChoice(oldRotation);
        }
        showStatus("Could not save rotation.");
    }
    private void setRotationChoice(int rotation) {
        updatingRotationChoice = true;
        comboRotationDegrees.getSelectionModel().select(Integer.valueOf(normalizeRotation(rotation)));
        updatingRotationChoice = false;
    }
    private Integer parseRotationInput() {
        String text = comboRotationDegrees.getEditor().getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return normalizeRotation(Integer.parseInt(text.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private int normalizeRotation(int rotation) {
        int normalized = rotation % 360;
        return normalized < 0 ? normalized + 360 : normalized;
    }
    // ===== Delete and reorder pages =====

    @FXML
    private void deleteCurrentPage() {
        // Deleting a page removes it from the database and then refreshes the current box.
        // The refresh keeps document/page counters correct after delete.
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
            showError("Delete failed.", e);
        }
    }
    private boolean reorderPage(int fromIndex, int toIndex) {
        if (selectedDocument == null || fromIndex < 0 || toIndex < 0
                || fromIndex >= currentPages.size() || toIndex > currentPages.size()) {
            return false;
        }

        Page movedPage = currentPages.remove(fromIndex);
        int insertIndex = toIndex > fromIndex ? toIndex - 1 : toIndex;

        if (insertIndex == fromIndex) {
            currentPages.add(fromIndex, movedPage);
            return false;
        }

        currentPages.add(insertIndex, movedPage);
        try {
            if (!scanWorkspaceManager.updatePageOrders(selectedDocument.getId(), currentPages)) {
                throw new Exception("Database did not accept the new page order.");
            }
            currentPageIndex = insertIndex;
            pagesByDocument.put(selectedDocument.getId(), copyPages(currentPages));
            renderDocumentCards();
            renderFilmstrip();
            showCurrentPage();
            showStatus("Page order saved.");
            return true;
        } catch (Exception e) {
            showStatus("Could not save page order.");
            showError("Could not save page order.", e);
            loadCurrentBoxDataAsync(true);
            return false;
        }
    }
    private boolean movePageInDocumentTree(
            UUID sourceDocumentId,
            UUID pageId,
            int sourcePageIndex,
            UUID targetDocumentId,
            int targetPageIndex
    ) {
        if (sourceDocumentId.equals(targetDocumentId)) {
            int targetIndex = targetPageIndex < 0
                    ? pagesByDocument.getOrDefault(sourceDocumentId, List.of()).size()
                    : targetPageIndex;
            return reorderPage(sourcePageIndex, targetIndex);
        }

        List<Page> sourcePages = copyPages(pagesByDocument.get(sourceDocumentId));
        List<Page> targetPages = copyPages(pagesByDocument.get(targetDocumentId));

        if (sourcePageIndex < 0 || sourcePageIndex >= sourcePages.size()) {
            return false;
        }

        int insertIndex = targetPageIndex < 0 ? targetPages.size() : Math.min(targetPageIndex, targetPages.size());
        Page movedPage = sourcePages.remove(sourcePageIndex);
        targetPages.add(insertIndex, movedPage);

        try {
            if (!scanWorkspaceManager.movePageBetweenDocuments(sourceDocumentId, targetDocumentId, sourcePages, targetPages)) {
                throw new Exception("Database did not accept the page move.");
            }

            pagesByDocument.put(targetDocumentId, copyPages(targetPages));
            if (sourcePages.isEmpty()) {
                pagesByDocument.remove(sourceDocumentId);
                currentDocuments.removeIf(document -> document.getId().equals(sourceDocumentId));
            } else {
                pagesByDocument.put(sourceDocumentId, copyPages(sourcePages));
            }

            selectDocument(targetDocumentId, insertIndex);
            showStatus("Page moved.");
            return true;
        } catch (Exception e) {
            showStatus("Could not move page.");
            showError("Could not move page.", e);
            loadCurrentBoxDataAsync(true);
            return false;
        }
    }

    // ===== Small helpers =====

    private void showStatus(String message) {
        labelConnected.setText(message);
    }
    private void runInBackground(Runnable task, String threadName) {
        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
    }
    private void showTaskError(String message, Task<?> task) {
        showError(message, task.getException());
    }
    private void showError(String message, Throwable error) {
        Window owner = scanRoot != null && scanRoot.getScene() != null ? scanRoot.getScene().getWindow() : null;
        ErrorDialog.show(owner, message, error);
    }
    private void writeLog(String action, String tableName, UUID recordId, String oldValue, String newValue) {
        // Scanning actions are also saved to the audit log.
        // This helps admin see who opened boxes and scanned files.
        logManager.createLog(getCurrentUserId(), action, tableName, recordId, oldValue, newValue);
    }
    private void openStartScanPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eksam/weblagereksam/Start-Scan-Popup.fxml"));
            Parent content = loader.load();
            StartScanPopupController controller = loader.getController();
            controller.setup(availableClients, availableProfiles);

            Stage popup = new Stage();
            popup.setTitle("Start scan");
            popup.initModality(Modality.APPLICATION_MODAL);
            Window owner = scanRoot.getScene() != null ? scanRoot.getScene().getWindow() : null;
            if (owner != null) {
                popup.initOwner(owner);
            }

            popup.setResizable(false);
            popup.setScene(new Scene(content));
            popup.showAndWait();

            if (controller.wasStarted()) {
                openBoxAsync(controller.getBoxNumber(), controller.getSelectedProfile());
            }
        } catch (IOException e) {
            showStatus("Could not open start scan popup.");
            showError("Could not open start scan popup.", e);
        }
    }
    private void openMyBoxesPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eksam/weblagereksam/My-Boxes-Popup.fxml"));
            Parent content = loader.load();
            MyBoxesPopupController controller = loader.getController();
            controller.setup(savedBoxes, currentBox, selectedExportFormat, currentDocuments.size());

            Stage popup = new Stage();
            popup.setTitle("My boxes");
            popup.initModality(Modality.APPLICATION_MODAL);
            Window owner = scanRoot.getScene() != null ? scanRoot.getScene().getWindow() : null;
            if (owner != null) {
                popup.initOwner(owner);
            }

            popup.setResizable(false);
            popup.setScene(new Scene(content));
            popup.showAndWait();
            handleMyBoxesAction(controller, owner);
        } catch (IOException e) {
            showStatus("Could not open my boxes popup.");
            showError("Could not open my boxes popup.", e);
        }
    }
    private void handleMyBoxesAction(MyBoxesPopupController controller, Window owner) {
        switch (controller.getAction()) {
            case OPEN_SAVED -> openSavedBox(controller.getSelectedBox());
            case DONE -> handleCompleteCurrentBox();
            case REMOVE -> handleRemoveCurrentBox(controller.getSelectedBox());
            case EXPORT -> {
                selectedExportFormat = controller.getSelectedFormat();
                scanExportHelper.export(currentBox, Session.getUser(), selectedExportFormat, owner, btnMyBoxes, this::showStatus, this::handleCompleteCurrentBox);
            }
            case NONE -> { }
        }
    }
    private record StartupData(List<Client> clients, List<Profile> profiles, List<Box> savedBoxes) {}
    private record PageImageLoad(Page page, Image image) {}
}
