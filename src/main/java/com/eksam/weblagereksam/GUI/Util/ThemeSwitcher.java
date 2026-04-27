package com.eksam.weblagereksam.GUI.Util;

import javafx.scene.Parent;
import javafx.scene.control.Button;

public class ThemeSwitcher {

    private static final String DARK_CSS = "/com/eksam/weblagereksam/Dark-mode.css";
    private static final String LIGHT_CSS = "/com/eksam/weblagereksam/Light-mode.css";

    private boolean darkMode = true;

    public void toggleTheme(Parent root, Button button) {
        darkMode = !darkMode;
        applyTheme(root, button);
    }

    private void applyTheme(Parent root, Button button) {
        String darkCss = getClass().getResource(DARK_CSS).toExternalForm();
        String lightCss = getClass().getResource(LIGHT_CSS).toExternalForm();

        root.getStylesheets().remove(darkCss);
        root.getStylesheets().remove(lightCss);
        root.getStylesheets().add(darkMode ? darkCss : lightCss);

        button.setText(darkMode ? "Light mode" : "Dark mode");
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}
