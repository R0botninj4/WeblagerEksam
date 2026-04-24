package com.eksam.weblagereksam.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Placeholder controller generated with the JavaFX project.
 *
 * It is currently not part of the scanning flow.
 */
public class HelloController {

    // ===== FXML controls =====

    @FXML
    private Label welcomeText;

    // ===== Demo action =====

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
