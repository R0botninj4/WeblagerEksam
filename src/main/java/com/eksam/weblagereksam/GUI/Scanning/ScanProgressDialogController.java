package com.eksam.weblagereksam.GUI.Scanning;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

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
        labelStatus.textProperty().unbind();
        progressBar.progressProperty().unbind();
    }
}
