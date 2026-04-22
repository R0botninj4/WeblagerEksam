package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BLL.BoxManager;
import com.eksam.weblagereksam.BLL.ScanImportManager;
import com.eksam.weblagereksam.GUI.Login.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.UUID;

public class UserScanningController {

    // ===== TOP =====
    @FXML private Label labelBoxId;
    @FXML private Label labelProfile;
    @FXML private Label labelFilesCount;
    @FXML private Label labelDocsCount;
    @FXML private Label labelSessionTimer;
    @FXML private Label labelUser;

    // ===== LEFT =====
    @FXML private Label labelDocCount;
    @FXML private VBox documentsContainer;
    @FXML private Label labelOutputName;
    @FXML private Label labelFormat;

    @FXML private Button btnMultiPage;
    @FXML private Button btnSinglePage;
    @FXML private Button btnExport;

    // ===== CENTER =====
    @FXML private Button btnRotateCCW;
    @FXML private Button btnRotateCW;
    @FXML private Button btnDelete;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnSlideshow;
    @FXML private Button btnNavLeft;
    @FXML private Button btnNavRight;

    @FXML private Label labelPagePosition;
    @FXML private Label labelPageRef;
    @FXML private ImageView pageImageView;
    @FXML private HBox filmstripBox;
    @FXML private Button btnFetchNext;

    // ===== BOTTOM =====
    @FXML private Label labelConnected;
    @FXML private Label labelRotationInfo;
    @FXML private Label labelStatusUser;

    private Box currentBox;

    @FXML
    public void initialize() {
        try {
            setupUserInfo();
            loadBox();
            setupDefaultUi();
            setupButtons();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupUserInfo() {
        if (Session.getUser() != null) {
            labelUser.setText(Session.getUser().getUsername());
            labelStatusUser.setText(Session.getUser().getUsername() + " · Scanning");
        } else {
            labelUser.setText("Unknown");
            labelStatusUser.setText("Unknown · Scanning");
        }
    }

    private void loadBox() throws Exception {
        BoxManager boxManager = new BoxManager();
        currentBox = boxManager.getBoxByBoxNumber("BOX-001");

        if (currentBox != null) {
            labelBoxId.setText(currentBox.getBoxNumber());
            labelProfile.setText(currentBox.getLabel() != null ? currentBox.getLabel() : "No profile");
            labelOutputName.setText(currentBox.getBoxNumber());
        } else {
            labelBoxId.setText("No Box");
            labelProfile.setText("No Profile");
            labelOutputName.setText("No Box");
        }
    }

    private void setupDefaultUi() {
        labelFilesCount.setText("Files 0");
        labelDocsCount.setText("Docs 0");
        labelDocCount.setText("0 docs");
        labelSessionTimer.setText("00:00");
        labelFormat.setText("TIFF");
        labelPagePosition.setText("0 / 0");
        labelPageRef.setText("No page loaded");
        labelConnected.setText("● Connected");
        labelRotationInfo.setText("Rotation: 0°");
    }

    private void setupButtons() {
        btnFetchNext.setOnAction(e -> handleFetchNext());

        btnMultiPage.setOnAction(e -> labelFormat.setText("TIFF Multi-page"));
        btnSinglePage.setOnAction(e -> labelFormat.setText("TIFF Single-page"));

        btnRotateCCW.setOnAction(e -> System.out.println("Rotate CCW"));
        btnRotateCW.setOnAction(e -> System.out.println("Rotate CW"));
        btnDelete.setOnAction(e -> System.out.println("Delete"));
        btnPrev.setOnAction(e -> System.out.println("Prev"));
        btnNext.setOnAction(e -> System.out.println("Next"));
        btnSlideshow.setOnAction(e -> System.out.println("Slideshow"));
        btnNavLeft.setOnAction(e -> System.out.println("Nav Left"));
        btnNavRight.setOnAction(e -> System.out.println("Nav Right"));
        btnExport.setOnAction(e -> System.out.println("Export"));
    }

    private void handleFetchNext() {
        try {
            if (currentBox == null) {
                System.out.println("No box selected.");
                return;
            }

            ScanImportManager scanImportManager = new ScanImportManager();
            UUID documentId = scanImportManager.importRandomTiffToBox(currentBox.getId());

            System.out.println("Imported document: " + documentId);

            // midlertidig UI update
            labelDocsCount.setText("Docs +1");
            labelDocCount.setText("Imported 1 doc");
            labelPageRef.setText("TIFF imported to DB");
            labelFilesCount.setText("Files imported");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}