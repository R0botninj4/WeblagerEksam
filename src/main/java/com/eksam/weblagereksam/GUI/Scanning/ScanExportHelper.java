package com.eksam.weblagereksam.GUI.Scanning;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.ExportManager;
import com.eksam.weblagereksam.BLL.Manager.ExportManager.ExportFormat;
import com.eksam.weblagereksam.GUI.Util.ErrorDialog;
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
        export(box, user, format, owner, exportButton, showStatus, null);
    }

    public void export(Box box, User user, ExportFormat format, Window owner, Button exportButton, Consumer<String> showStatus, Runnable afterExport) {
        if (box == null) {
            showStatus.accept("Open a box before exporting.");
            return;
        }

        File folder = chooseExportFolder(owner);
        if (folder == null) {
            showStatus.accept("Export cancelled.");
            return;
        }

        startExport(box, user, format, folder.toPath(), owner, exportButton, showStatus, afterExport);
    }

    private File chooseExportFolder(Window owner) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose export folder");
        return chooser.showDialog(owner);
    }

    private void startExport(Box box, User user, ExportFormat format, Path folder, Window owner, Button exportButton, Consumer<String> showStatus, Runnable afterExport) {
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
            if (afterExport != null) {
                afterExport.run();
            }
        });

        exportTask.setOnFailed(event -> {
            exportButton.setDisable(false);
            showStatus.accept("Export failed.");
            Throwable error = exportTask.getException();
            ErrorDialog.show(owner, "Export failed.", error);
        });

        Thread thread = new Thread(exportTask, "scan-export-thread");
        thread.setDaemon(true);
        thread.start();
    }
}
