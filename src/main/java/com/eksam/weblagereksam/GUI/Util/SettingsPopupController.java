package com.eksam.weblagereksam.GUI.Util;

import com.eksam.weblagereksam.GUI.Interface.ClosableWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class SettingsPopupController implements ClosableWindow {

    @FXML private Button btnThemeSwitch;
    @FXML private Label labelThemeMode;
    @FXML private VBox shortcutsBox;
    @FXML private Button btnClose;

    private Consumer<Boolean> themeChanged;
    private Runnable logoutAction;
    private boolean darkMode;

    public void setup(boolean darkMode, Consumer<Boolean> themeChanged, Runnable logoutAction, List<ShortcutList.Shortcut> shortcuts) {
        this.themeChanged = themeChanged;
        this.logoutAction = logoutAction;
        this.darkMode = darkMode;
        shortcutsBox.getChildren().clear();
        updateThemeSwitch();
        enableEscClose(btnClose);

        String currentGroup = "";
        for (ShortcutList.Shortcut shortcut : shortcuts) {
            if (!shortcut.group().equals(currentGroup)) {
                currentGroup = shortcut.group();
                Label heading = new Label(currentGroup);
                heading.getStyleClass().add("settings-shortcut-heading");
                shortcutsBox.getChildren().add(heading);
            }

            shortcutsBox.getChildren().add(createShortcutRow(shortcut));
        }
    }

    @FXML
    private void handleDarkModeChanged() {
        darkMode = !darkMode;
        updateThemeSwitch();
        if (themeChanged != null) {
            themeChanged.accept(darkMode);
        }
    }

    private void updateThemeSwitch() {
        btnThemeSwitch.getStyleClass().setAll("theme-switch");
        btnThemeSwitch.getStyleClass().add(darkMode ? "theme-switch-dark" : "theme-switch-light");
        btnThemeSwitch.setText(darkMode ? "Dark" : "Light");
        labelThemeMode.setText(darkMode ? "Dark mode" : "Light mode");
    }

    private HBox createShortcutRow(ShortcutList.Shortcut shortcut) {
        Label keys = new Label(shortcut.keys());
        keys.getStyleClass().add("settings-shortcut-key");

        Label action = new Label(shortcut.action());
        action.getStyleClass().add("settings-shortcut-action");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, keys, spacer, action);
        row.getStyleClass().add("settings-shortcut-row");
        return row;
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        closeWindow(event);
        if (logoutAction != null) {
            logoutAction.run();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        closeWindow(event);
    }
}
