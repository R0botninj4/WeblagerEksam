package com.eksam.weblagereksam.GUI.Admin;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import com.eksam.weblagereksam.BLL.Manager.ProfileManager;
import com.eksam.weblagereksam.BLL.Manager.UserManager;
import com.eksam.weblagereksam.GUI.Login.Session;
import com.eksam.weblagereksam.GUI.Util.LogoutHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class AdminController {

    @FXML private Button btnAttendance, btnUsers, btnProfiles, btnClients, btnLogged;
    @FXML private TextField txtSearch;
    @FXML private TableView<Object> tableAdmin;

    private UserManager userManager;
    private ProfileManager profileManager;
    private ClientManager clientManager;
    private LogoutHelper logoutHelper;
    private AdminPage currentPage = AdminPage.USERS;

    @FXML
    public void initialize() {
        btnLogged.setDisable(true);
        btnLogged.setVisible(false);
        try {
            userManager = new UserManager();
            profileManager = new ProfileManager();
            clientManager = new ClientManager();
            logoutHelper = new LogoutHelper();

            showUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showAttendance() {
        txtSearch.setPromptText("Search attendance");
        currentPage = AdminPage.ATTENDANCE;
        setActiveButton(btnAttendance);

        tableAdmin.getColumns().setAll(
                textColumn("Username", row -> ((User) row).getUsername()),
                textColumn("Full name", row -> ((User) row).getFullName()),
                textColumn("Logged in now", row -> isLoggedInNow((User) row) ? "Yes" : "No"),
                textColumn("Last login", row -> formatDateTime(((User) row).getLastLogin()))
        );

        tableAdmin.setItems(FXCollections.observableArrayList(userManager.getAllUsers()));
    }

    @FXML
    private void showUsers() {
        txtSearch.setPromptText("Search user");
        currentPage = AdminPage.USERS;
        setActiveButton(btnUsers);

        tableAdmin.getColumns().setAll(
                textColumn("Username", row -> ((User) row).getUsername()),
                textColumn("Full name", row -> ((User) row).getFullName()),
                textColumn("Role", row -> ((User) row).getRoleName()),
                textColumn("Active", row -> ((User) row).isActive() ? "Yes" : "No")
        );

        tableAdmin.setItems(FXCollections.observableArrayList(userManager.getAllUsers()));
    }

    @FXML
    private void showProfiles() {
        txtSearch.setPromptText("Search profile");
        currentPage = AdminPage.PROFILES;
        setActiveButton(btnProfiles);

        tableAdmin.getColumns().setAll(
                textColumn("Name", row -> ((Profile) row).getName()),
                textColumn("Client", row -> ((Profile) row).getClientName()),
                textColumn("Barcode rule", row -> ((Profile) row).getBarcodeSplitRule()),
                textColumn("Created", row -> formatDate(((Profile) row).getCreatedAt()))
        );

        tableAdmin.setItems(FXCollections.observableArrayList(profileManager.getAllProfiles()));
    }

    @FXML
    private void showClients() {
        txtSearch.setPromptText("Search client");
        currentPage = AdminPage.CLIENTS;
        setActiveButton(btnClients);

        tableAdmin.getColumns().setAll(
                textColumn("Name", row -> ((Client) row).getName()),
                textColumn("Code", row -> ((Client) row).getCode()),
                textColumn("Created", row -> formatDate(((Client) row).getCreatedAt()))
        );

        tableAdmin.setItems(FXCollections.observableArrayList(clientManager.getAllClients()));
    }

    @FXML
    private void handleAdd() {
        if (currentPage == AdminPage.ATTENDANCE) {
            showInfo("Attendance is only for viewing login status.");
            return;
        }

        openCurrentPagePopup("Add");
    }

    @FXML
    private void handleEdit() {
        if (currentPage == AdminPage.ATTENDANCE) {
            showInfo("Attendance is only for viewing login status.");
            return;
        }

        if (tableAdmin.getSelectionModel().getSelectedItem() == null) {
            showInfo("Select a row before editing.");
            return;
        }

        openCurrentPagePopup("Edit");
    }

    @FXML
    private void handleDelete() {
        if (currentPage == AdminPage.ATTENDANCE) {
            showInfo("Attendance is only for viewing login status.");
            return;
        }

        Object selectedRow = tableAdmin.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showInfo("Select a row before deleting.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete " + currentPage.singularName);
        alert.setHeaderText("Delete selected " + currentPage.singularName.toLowerCase() + "?");
        alert.setContentText("This opens the delete popup for the current admin page.");

        Window owner = tableAdmin.getScene() != null ? tableAdmin.getScene().getWindow() : null;
        if (owner != null) {
            alert.initOwner(owner);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteSelectedRow(selectedRow);
        }
    }

    @FXML
    private void handleRefresh() {
        refreshCurrentPage();
    }

    @FXML
    private void handleLogout() {
        logoutHelper.logout(tableAdmin.getScene().getWindow());
    }

    private void openCurrentPagePopup(String action) {
        String title = action + " " + currentPage.singularName;
        Object selectedRow = "Edit".equals(action) ? tableAdmin.getSelectionModel().getSelectedItem() : null;
        Parent content;
        AdminPopupController popupController;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(currentPage.fxmlPath));
            content = loader.load();
            popupController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Could not open " + currentPage.singularName.toLowerCase() + " popup.");
            return;
        }

        popupController.setup(action, selectedRow);

        Stage popup = new Stage();
        popup.setTitle(title);
        popup.initModality(Modality.APPLICATION_MODAL);

        Window owner = tableAdmin.getScene() != null ? tableAdmin.getScene().getWindow() : null;
        if (owner != null) {
            popup.initOwner(owner);
        }

        popup.setResizable(false);
        popup.setScene(new Scene(content));
        popup.showAndWait();

        if (popupController.wasSaved()) {
            refreshCurrentPage();
        }
    }

    private void deleteSelectedRow(Object selectedRow) {
        boolean deleted = switch (currentPage) {
            case ATTENDANCE -> false;
            case USERS -> userManager.deleteUser(((User) selectedRow).getId());
            case PROFILES -> profileManager.deleteProfile(((Profile) selectedRow).getId());
            case CLIENTS -> clientManager.deleteClient(((Client) selectedRow).getId());
        };

        if (deleted) {
            refreshCurrentPage();
        } else {
            showInfo("Could not delete selected " + currentPage.singularName.toLowerCase() + ".");
        }
    }

    private void refreshCurrentPage() {
        switch (currentPage) {
            case ATTENDANCE -> showAttendance();
            case USERS -> showUsers();
            case PROFILES -> showProfiles();
            case CLIENTS -> showClients();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private TableColumn<Object, String> textColumn(String title, TextGetter getter) {
        TableColumn<Object, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(safeText(getter.getText(cell.getValue()))));
        return column;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "-" : date.toLocalDate().toString();
    }

    private String formatDateTime(LocalDateTime date) {
        return date == null ? "Never" : date.toString().replace("T", " ");
    }

    private boolean isLoggedInNow(User user) {
        if (Session.getUser() != null && Session.getUser().getId().equals(user.getId())) {
            return true;
        }

        if (user.getLastLogin() == null) {
            return false;
        }

        return Duration.between(user.getLastLogin(), LocalDateTime.now()).toMinutes() < 30;
    }

    private void setActiveButton(Button activeButton) {
        btnAttendance.getStyleClass().remove("nav-btn-active");
        btnUsers.getStyleClass().remove("nav-btn-active");
        btnProfiles.getStyleClass().remove("nav-btn-active");
        btnClients.getStyleClass().remove("nav-btn-active");
        activeButton.getStyleClass().add("nav-btn-active");
    }

    @FunctionalInterface
    private interface TextGetter {
        String getText(Object row);
    }

    private enum AdminPage {
        ATTENDANCE("Attendance", ""),
        USERS("User", "/com/eksam/weblagereksam/Admin-Create-User-Popup.fxml"),
        PROFILES("Profile", "/com/eksam/weblagereksam/Admin-Create-Profile-Popup.fxml"),
        CLIENTS("Client", "/com/eksam/weblagereksam/Admin-Create-Client-Popup.fxml");

        private final String singularName;
        private final String fxmlPath;

        AdminPage(String singularName, String fxmlPath) {
            this.singularName = singularName;
            this.fxmlPath = fxmlPath;
        }
    }
}
