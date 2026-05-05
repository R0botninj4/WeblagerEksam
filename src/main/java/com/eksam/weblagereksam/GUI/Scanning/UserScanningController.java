package com.eksam.weblagereksam.GUI.Scanning;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.BLL.Image.FxImageConverter;
import com.eksam.weblagereksam.BLL.Manager.BoxManager;
import com.eksam.weblagereksam.BLL.Manager.ProfileManager;
import com.eksam.weblagereksam.BLL.Manager.ScanImportManager;
import com.eksam.weblagereksam.BLL.Manager.ScanWorkspaceManager;
import com.eksam.weblagereksam.BLL.Manager.ScanWorkspaceManager.BoxDataSnapshot;
import com.eksam.weblagereksam.GUI.Login.Session;
import com.eksam.weblagereksam.GUI.Renderer.ScanViewRenderer;
import com.eksam.weblagereksam.GUI.Util.BoxDisplayConverter;
import com.eksam.weblagereksam.GUI.Util.LogoutHelper;
import com.eksam.weblagereksam.GUI.Util.ThemeSwitcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    // ===== FXML: Top bar and status labels =====

    @FXML private Label labelClient, labelProfile, labelFilesCount, labelDocsCount, labelUser, labelDocCount;
    @FXML private Label labelOutputName, labelFormat, labelPagePosition, labelPageRef;
    @FXML private Label labelConnected, labelRotationInfo, labelStatusUser;
    @FXML private TextField txtBoxNumber;
    @FXML private ComboBox<Profile> comboProfile;
    @FXML private ComboBox<Box> comboSavedBoxes;
    @FXML private BorderPane scanRoot;

    // ===== FXML: Action buttons =====

    @FXML private Button btnExport, btnRotateCCW, btnRotateCW, btnDeletePage, btnPrev;
    @FXML private Button btnNext, btnNavLeft, btnNavRight, btnFetchNext, btnFetchTen;
    @FXML private Button btnThemeToggle, btnOpenBox, btnOpenSavedBox, btnDoneBox, btnRemoveBox;

    // ===== FXML: Main content containers =====

    @FXML private VBox documentsContainer;
    @FXML private HBox filmstripBox;
    @FXML private ImageView pageImageView;

    // ===== BLL / GUI helpers =====

    private BoxManager boxManager;
    private ProfileManager profileManager;
    private ScanImportManager scanImportManager;
    private ScanWorkspaceManager scanWorkspaceManager;
    private ScanViewRenderer scanViewRenderer;
    private ThemeSwitcher themeSwitcher;
    private LogoutHelper logoutHelper;

    // ===== Current screen state =====

    private Box currentBox;
    private Document selectedDocument;
    private final List<Document> currentDocuments = new ArrayList<>();
    private final List<Page> currentPages = new ArrayList<>();
    private final Map<UUID, List<Page>> pagesByDocument = new HashMap<>();

    private final Map<UUID, Image> pageImageCache = new HashMap<>();
    private final Map<UUID, VBox> filmstripThumbs = new HashMap<>();
    private final Set<UUID> loadingPageIds = new HashSet<>();

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
            profileManager = new ProfileManager();
            scanImportManager = new ScanImportManager();
            scanWorkspaceManager = new ScanWorkspaceManager();
            scanViewRenderer = new ScanViewRenderer();
            themeSwitcher = new ThemeSwitcher();
            logoutHelper = new LogoutHelper();

            setupUserInfo();
            setupKeyboardShortcuts();
            setupSavedBoxes();
            showNoBoxSelected();
            loadStartupDataAsync();
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

    // ===== FXML actions: box and toolbar controls =====

    @FXML
    private void handleOpenBox() {
        String boxNumber = txtBoxNumber.getText();
        Profile selectedProfile = comboProfile.getSelectionModel().getSelectedItem();

        if (boxNumber == null || boxNumber.isBlank()) {
            showStatus("Enter a box number first.");
            return;
        }

        if (selectedProfile == null) {
            showStatus("Select a profile first.");
            return;
        }

        openBoxAsync(boxNumber.trim(), selectedProfile);
    }

    private void openBoxAsync(String boxNumber, Profile selectedProfile) {
        Task<Box> openTask = new Task<>() {
            @Override
            protected Box call() throws Exception {
                Box box = boxManager.getBoxByBoxNumber(boxNumber);

                if (box == null) {
                    box = createBoxForProfile(boxNumber, selectedProfile);
                } else {
                    box = updateBoxProfile(box, selectedProfile);
                }

                UUID userId = getCurrentUserId();
                if (userId != null) {
                    boxManager.assignBoxToUser(userId, box.getId());
                }

                return box;
            }
        };

        openTask.setOnRunning(event -> {
            setDisabled(true, btnOpenSavedBox, btnOpenBox, btnDoneBox, btnRemoveBox);
            showStatus("Opening box...");
        });

        openTask.setOnSucceeded(event -> {
            setDisabled(false, btnOpenSavedBox, btnOpenBox, btnDoneBox, btnRemoveBox);
            Box box = openTask.getValue();
            loadSavedBoxesForCurrentUserAsync(box);
            selectSavedBox(box);
            openBox(box);
        });

        openTask.setOnFailed(event -> {
            setDisabled(false, btnOpenSavedBox, btnOpenBox, btnDoneBox, btnRemoveBox);
            showStatus("Could not open box.");
            Throwable error = openTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread openThread = new Thread(openTask, "scan-open-box-thread");
        openThread.setDaemon(true);
        openThread.start();
    }

    private Box createBoxForProfile(String boxNumber, Profile profile) throws Exception {
        Box box = new Box(
                null,
                profile.getClientId(),
                profile.getId(),
                profile.getClientName(),
                profile.getName(),
                boxNumber,
                boxNumber,
                "IN_PROGRESS",
                null
        );

        UUID boxId = boxManager.createBox(box);

        if (boxId == null) {
            throw new Exception("Could not create box.");
        }

        return boxManager.getBoxById(boxId);
    }

    private Box updateBoxProfile(Box box, Profile profile) {
        box.setClientId(profile.getClientId());
        box.setProfileId(profile.getId());
        box.setClientName(profile.getClientName());
        box.setProfileName(profile.getName());
        box.setStatus("IN_PROGRESS");
        boxManager.updateBox(box);
        return box;
    }

    private void openBox(Box box) {
        currentBox = box;
        selectedDocument = null;
        currentPageIndex = 0;
        currentDocuments.clear();
        currentPages.clear();
        pagesByDocument.clear();
        filmstripThumbs.clear();
        txtBoxNumber.setText(box.getBoxNumber());
        selectProfileForBox(box);
        selectSavedBox(box);
        updateBoxHeader();
        loadCurrentBoxDataAsync(false);
    }

    private void setupSavedBoxes() {
        comboSavedBoxes.setConverter(new BoxDisplayConverter());
    }

    private void loadSavedBoxesForCurrentUserAsync(Box boxToSelect) {
        Task<List<Box>> loadTask = new Task<>() {
            @Override
            protected List<Box> call() {
                UUID userId = getCurrentUserId();
                return userId != null ? boxManager.getBoxesByUserId(userId) : List.of();
            }
        };

        loadTask.setOnSucceeded(event -> {
            comboSavedBoxes.getItems().setAll(loadTask.getValue());
            selectSavedBox(boxToSelect);
        });

        loadTask.setOnFailed(event -> {
            Throwable error = loadTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread loadThread = new Thread(loadTask, "scan-saved-boxes-load-thread");
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void loadStartupDataAsync() {
        Task<StartupData> startupTask = new Task<>() {
            @Override
            protected StartupData call() {
                UUID userId = getCurrentUserId();
                List<Box> savedBoxes = userId != null ? boxManager.getBoxesByUserId(userId) : List.of();
                return new StartupData(profileManager.getAllProfiles(), savedBoxes);
            }
        };

        startupTask.setOnRunning(event -> showStatus("Loading scanner data..."));

        startupTask.setOnSucceeded(event -> {
            StartupData data = startupTask.getValue();
            comboProfile.getItems().setAll(data.profiles());
            comboSavedBoxes.getItems().setAll(data.savedBoxes());
            selectSavedBox(currentBox);
            showStatus("Enter box number and select profile.");
        });

        startupTask.setOnFailed(event -> {
            showStatus("Could not load scanner data.");
            Throwable error = startupTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread startupThread = new Thread(startupTask, "scan-startup-load-thread");
        startupThread.setDaemon(true);
        startupThread.start();
    }

    private UUID getCurrentUserId() {
        return Session.getUser() != null ? Session.getUser().getId() : null;
    }

    private void selectSavedBox(Box box) {
        if (box == null) {
            comboSavedBoxes.getSelectionModel().clearSelection();
            return;
        }

        comboSavedBoxes.getItems().stream()
                .filter(savedBox -> savedBox.getId().equals(box.getId()))
                .findFirst()
                .ifPresent(savedBox -> comboSavedBoxes.getSelectionModel().select(savedBox));
    }

    private void selectProfileForBox(Box box) {
        if (box.getProfileId() == null) {
            comboProfile.getSelectionModel().clearSelection();
            return;
        }

        comboProfile.getItems().stream()
                .filter(profile -> box.getProfileId().equals(profile.getId()))
                .findFirst()
                .ifPresent(profile -> comboProfile.getSelectionModel().select(profile));
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

    private void updateBoxHeader() {
        labelClient.setText("Client: " + (currentBox.getClientName() != null ? currentBox.getClientName() : "No client"));
        labelProfile.setText("Profile: " + (currentBox.getProfileName() != null ? currentBox.getProfileName() : "No profile"));
        labelOutputName.setText(currentBox.getBoxNumber());
    }

    private void showNoBoxSelected() {
        labelClient.setText("Client: No client");
        labelProfile.setText("Profile: No profile");
        labelOutputName.setText("No box");
        showStatus("Enter box number and select profile.");
    }

    @FXML private void handleFetchNext() { startImportTask(1); }

    @FXML private void handleFetchTen() { startImportTask(10); }

    @FXML private void handleRotateLeft() { rotateCurrentPage(-90); }

    @FXML private void handleRotateRight() { rotateCurrentPage(90); }

    @FXML private void handleMultiPageFormat() { labelFormat.setText("TIFF Multi-page"); }

    @FXML private void handleSinglePageFormat() { labelFormat.setText("TIFF Single-page"); }

    @FXML private void handleSlideshow() { showStatus("Slideshow is not ready yet."); }

    @FXML private void handleExport() { showStatus("Export is not ready yet."); }

    @FXML
    private void handleOpenSavedBox() {
        Box selectedBox = comboSavedBoxes.getSelectionModel().getSelectedItem();

        if (selectedBox == null) {
            showStatus("Select a saved box first.");
            return;
        }

        txtBoxNumber.setText(selectedBox.getBoxNumber());
        openBox(selectedBox);
    }

    @FXML
    private void handleCompleteCurrentBox() {
        if (currentBox == null) {
            showStatus("Open a box first.");
            return;
        }

        currentBox.setStatus("COMPLETED");
        completeBoxAsync(currentBox);
    }

    private void completeBoxAsync(Box box) {
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
            setDisabled(true, btnDoneBox, btnRemoveBox);
            showStatus("Marking box as done...");
        });

        completeTask.setOnSucceeded(event -> {
            setDisabled(false, btnDoneBox, btnRemoveBox);

            if (!completeTask.getValue()) {
                showStatus("Could not mark box as done.");
                return;
            }

            clearCurrentBox();
            loadSavedBoxesForCurrentUserAsync(null);
            showStatus("Box marked as done.");
        });

        completeTask.setOnFailed(event -> {
            setDisabled(false, btnDoneBox, btnRemoveBox);
            showStatus("Could not mark box as done.");
            Throwable error = completeTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread completeThread = new Thread(completeTask, "scan-complete-box-thread");
        completeThread.setDaemon(true);
        completeThread.start();
    }

    @FXML
    private void handleRemoveCurrentBox() {
        Box selectedSavedBox = comboSavedBoxes.getSelectionModel().getSelectedItem();
        Box boxToRemove = selectedSavedBox != null ? selectedSavedBox : currentBox;

        if (boxToRemove == null) {
            showStatus("Select a saved box first.");
            return;
        }

        removeBoxAsync(boxToRemove);
    }

    private void removeBoxAsync(Box boxToRemove) {
        Task<Void> removeTask = new Task<>() {
            @Override
            protected Void call() {
                removeBoxFromCurrentUser(boxToRemove);
                return null;
            }
        };

        removeTask.setOnRunning(event -> {
            setDisabled(true, btnDoneBox, btnRemoveBox);
            showStatus("Removing box from your list...");
        });

        removeTask.setOnSucceeded(event -> {
            setDisabled(false, btnDoneBox, btnRemoveBox);

            if (currentBox != null && currentBox.getId().equals(boxToRemove.getId())) {
                clearCurrentBox();
            }

            loadSavedBoxesForCurrentUserAsync(currentBox);
            showStatus("Box removed from your list.");
        });

        removeTask.setOnFailed(event -> {
            setDisabled(false, btnDoneBox, btnRemoveBox);
            showStatus("Could not remove box.");
            Throwable error = removeTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread removeThread = new Thread(removeTask, "scan-remove-box-thread");
        removeThread.setDaemon(true);
        removeThread.start();
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
        txtBoxNumber.clear();
        labelDocsCount.setText("Docs 0");
        labelDocCount.setText("0 docs");
        labelFilesCount.setText("Files 0");
        btnExport.setText("Export (0 documents)");
        renderDocumentCards();
        renderFilmstrip();
        showCurrentPage();
        showNoBoxSelected();
    }

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
        labelPagePosition.setText((currentPageIndex + 1) + " / " + currentPages.size());
        labelPageRef.setText(
                "Document " + selectedDocument.getDocumentNumber()
                        + " | "
                        + page.getFileName()
                        + (page.isBarcodePage() ? " | BARCODE" : "")
        );
        labelRotationInfo.setText("Rotation: " + page.getRotation() + " degrees");

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
                showStatus("Ready");
            }
        });

        loadTask.setOnFailed(event -> {
            loadingPageIds.remove(pageId);
            showStatus("Could not load page.");
            Throwable error = loadTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread loadThread = new Thread(loadTask, "scan-page-load-thread");
        loadThread.setDaemon(true);
        loadThread.start();
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

    // ===== Page actions =====

    private void rotateCurrentPage(int delta) {
        if (selectedDocument == null || currentPages.isEmpty()) {
            return;
        }

        try {
            Page page = currentPages.get(currentPageIndex);
            if (page.getImageData() == null) {
                showStatus("Wait for the page to load first.");
                return;
            }

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

    private record StartupData(List<Profile> profiles, List<Box> savedBoxes) {}

    private record PageImageLoad(Page page, Image image) {}
}
