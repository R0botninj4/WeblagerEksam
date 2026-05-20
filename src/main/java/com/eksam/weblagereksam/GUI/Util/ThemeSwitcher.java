package com.eksam.weblagereksam.GUI.Util;

import javafx.scene.Parent;

public class ThemeSwitcher {

    private static final String DARK_CSS = "/com/eksam/weblagereksam/Dark-mode.css";
    private static final String LIGHT_CSS = "/com/eksam/weblagereksam/Light-mode.css";

    private boolean darkMode = false;

    public void setDarkMode(Parent root, boolean darkMode) {
        this.darkMode = darkMode;
        applyTheme(root);
    }

    private void applyTheme(Parent root) {
        String darkCss = getClass().getResource(DARK_CSS).toExternalForm();
        String lightCss = getClass().getResource(LIGHT_CSS).toExternalForm();

        root.getStylesheets().remove(darkCss);
        root.getStylesheets().remove(lightCss);
        root.getStylesheets().add(darkMode ? darkCss : lightCss);
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}
