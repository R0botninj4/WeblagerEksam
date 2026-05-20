package com.eksam.weblagereksam.GUI.Interface;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public interface ClosableWindow {

    default void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    default void enableEscClose(Button closeButton) {
        Scene scene = closeButton.getScene();
        if (scene != null) {
            addEscHandler(scene, closeButton);
        } else {
            closeButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    addEscHandler(newScene, closeButton);
                }
            });
        }
    }

    static void enableEscClose(Stage stage) {
        Scene scene = stage.getScene();
        if (scene != null) {
            addEscHandler(scene, stage);
        } else {
            stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    addEscHandler(newScene, stage);
                }
            });
        }
    }

    private void addEscHandler(Scene scene, Button closeButton) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeWindow(new ActionEvent(closeButton, null));
                event.consume();
            }
        });
    }

    private static void addEscHandler(Scene scene, Stage stage) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
                event.consume();
            }
        });
    }
}
