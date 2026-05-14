package com.eksam.weblagereksam.GUI.Scanning;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ScanProgressPopup {

    private Stage popup;
    private ScanProgressDialogController controller;

    public boolean show(Task<?> task, int amount, Window owner) {
        close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eksam/weblagereksam/Scan-Progress-dialog.fxml"));
            VBox content = loader.load();

            controller = loader.getController();
            controller.bind(task, amount);

            popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                popup.initOwner(owner);
            }

            popup.setTitle("Scanning");
            popup.setResizable(false);
            popup.setScene(new Scene(content));
            popup.show();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (controller != null) {
            controller.unbind();
            controller = null;
        }

        if (popup != null) {
            popup.close();
            popup = null;
        }
    }
}
