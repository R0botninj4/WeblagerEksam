package com.eksam.weblagereksam.GUI;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Controller for Scan-Progress-dialog.fxml.
 *
 * It binds the popup labels/progress bar to the JavaFX Task that imports scans.
 */
public class ScanProgressDialogController {

    // ===== FXML controls =====

    @FXML private Label labelTitle;
    @FXML private Label labelStatus;
    @FXML private ProgressBar progressBar;

    // ===== Task binding =====

    public void bind(Task<?> task, int amount) {
        labelTitle.setText("Scanning " + amount + " files");
        labelStatus.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());
    }

    public void unbind() {
        // Unbinding prevents the finished background task from staying connected to the popup controls.
        labelStatus.textProperty().unbind();
        progressBar.progressProperty().unbind();
    }
}
