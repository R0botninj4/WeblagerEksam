package com.eksam.weblagereksam.GUI.Scanning;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.ExportManager;
import com.eksam.weblagereksam.BLL.Manager.ExportManager.ExportFormat;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ScanExportHelper {

    private final ExportManager exportManager;

    public ScanExportHelper() throws Exception {
        exportManager = new ExportManager();
    }

    public void export(Box box, User user, ExportFormat format, Window owner, Button exportButton, Consumer<String> showStatus) {
        if (box == null) {
            showStatus.accept("Open a box before exporting.");
            return;
        }

        File folder = chooseExportFolder(owner);
        if (folder == null) {
            showStatus.accept("Export cancelled.");
            return;
        }

        startExport(box, user, format, folder.toPath(), exportButton, showStatus);
    }

    private File chooseExportFolder(Window owner) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose export folder");
        return chooser.showDialog(owner);
    }

    private void startExport(Box box, User user, ExportFormat format, Path folder, Button exportButton, Consumer<String> showStatus) {
        Task<Path> exportTask = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return exportManager.exportBox(box, user, folder, format);
            }
        };

        exportTask.setOnRunning(event -> {
            exportButton.setDisable(true);
            showStatus.accept("Exporting box...");
        });

        exportTask.setOnSucceeded(event -> {
            exportButton.setDisable(false);
            showStatus.accept("Export completed: " + exportTask.getValue());
        });

        exportTask.setOnFailed(event -> {
            exportButton.setDisable(false);
            showStatus.accept("Export failed.");
            Throwable error = exportTask.getException();
            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread thread = new Thread(exportTask, "scan-export-thread");
        thread.setDaemon(true);
        thread.start();
    }
}
