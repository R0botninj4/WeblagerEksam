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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AdminController {

    private static final String ACTIVE_NAV_STYLE = """
            -fx-background-color: #2D3D4F;
            -fx-text-fill: white;
            -fx-border-color: white;
            -fx-border-width: 0 0 0 4;
            -fx-background-radius: 6;
            -fx-border-radius: 6;
            """;

    // FXML fields are connected to Admin-view.fxml.
    // That means JavaFX fills these variables when the view is loaded.
    @FXML private Button btnAttendance, btnUsers, btnProfiles, btnClients, btnLogged;
    @FXML private TextField txtSearch;
    @FXML private TableView<Object> tableAdmin;

    // Managers belong to the BLL layer.
    // The controller asks managers for data instead of talking directly to the database.
    private UserManager userManager;
    private ProfileManager profileManager;
    private ClientManager clientManager;
    private LogoutHelper logoutHelper;
    private List<Object> currentRows = new ArrayList<>();

    // Keeps track of which admin page/table is currently shown.
    // Example: USERS means Add/Edit/Delete should open the user popup.
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

            setupTableContextMenu();
            setupSearchBar();
            showUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearchBar() {
        // Every time the admin types in the search field, the current table is filtered.
        // The full list is kept in currentRows, so clearing search shows everything again.
        txtSearch.textProperty().addListener((obs, oldText, newText) -> applySearchFilter());
    }

    private void setupTableContextMenu() {
        // This is the menu shown when the admin right-clicks the table.
        // It reuses the same methods as the toolbar buttons, so the logic only exists once.
        MenuItem addItem = new MenuItem("Add");
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");

        addItem.setOnAction(event -> handleAdd());
        editItem.setOnAction(event -> handleEdit());
        deleteItem.setOnAction(event -> handleDelete());

        ContextMenu contextMenu = new ContextMenu(addItem, editItem, deleteItem);
        contextMenu.setOnShowing(event -> {
            // Attendance is read-only, so Add/Edit/Delete should not be clickable there.
            // Edit/Delete also need a selected row before they make sense.
            boolean attendancePage = currentPage == AdminPage.ATTENDANCE;
            boolean rowSelected = tableAdmin.getSelectionModel().getSelectedItem() != null;

            addItem.setDisable(attendancePage);
            editItem.setDisable(attendancePage || !rowSelected);
            deleteItem.setDisable(attendancePage || !rowSelected);
        });

        tableAdmin.setContextMenu(contextMenu);
    }

    @FXML
    private void showAttendance() {
        // Attendance shows login information only.
        // It uses User data, but it is not meant for editing users.
        txtSearch.setPromptText("Search attendance");
        currentPage = AdminPage.ATTENDANCE;
        setActiveButton(btnAttendance);

        tableAdmin.getColumns().setAll(
                textColumn("Username", row -> ((User) row).getUsername()),
                textColumn("Logged in now", row -> isLoggedInNow((User) row) ? "Yes" : "No"),
                textColumn("Last login", row -> formatDateTime(((User) row).getLastLogin()))
        );

        setTableRows(userManager.getAllUsers());
    }

    @FXML
    private void showUsers() {
        // Rebuilds the table so it fits the Users page.
        // Each column tells JavaFX which value to show from a User object.
        txtSearch.setPromptText("Search user");
        currentPage = AdminPage.USERS;
        setActiveButton(btnUsers);

        tableAdmin.getColumns().setAll(
                textColumn("Username", row -> ((User) row).getUsername()),
                textColumn("Role", row -> ((User) row).getRoleName()),
                textColumn("Active", row -> ((User) row).isActive() ? "Yes" : "No")
        );

        setTableRows(userManager.getAllUsers());
    }

    @FXML
    private void showProfiles() {
        // Rebuilds the table so it fits the Profiles page.
        txtSearch.setPromptText("Search profile");
        currentPage = AdminPage.PROFILES;
        setActiveButton(btnProfiles);

        tableAdmin.getColumns().setAll(
                textColumn("Name", row -> ((Profile) row).getName()),
                textColumn("Client", row -> ((Profile) row).getClientName()),
                textColumn("Created", row -> formatDate(((Profile) row).getCreatedAt()))
        );

        setTableRows(profileManager.getAllProfiles());
    }

    @FXML
    private void showClients() {
        // Rebuilds the table so it fits the Clients page.
        txtSearch.setPromptText("Search client");
        currentPage = AdminPage.CLIENTS;
        setActiveButton(btnClients);

        tableAdmin.getColumns().setAll(
                textColumn("Name", row -> ((Client) row).getName()),
                textColumn("Created", row -> formatDate(((Client) row).getCreatedAt()))
        );

        setTableRows(clientManager.getAllClients());
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
        boolean userPage = currentPage == AdminPage.USERS;
        alert.setTitle((userPage ? "Deactivate " : "Delete ") + currentPage.singularName);
        alert.setHeaderText((userPage ? "Deactivate selected " : "Delete selected ") + currentPage.singularName.toLowerCase() + "?");
        alert.setContentText(userPage
                ? "The user will stay in the database, but they can no longer log in."
                : "This will delete the selected " + currentPage.singularName.toLowerCase() + ".");

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
        // Opens the correct popup for the page we are currently on.
        // Example: if currentPage is USERS, this opens Admin-Create-User-Popup.fxml.
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

        // Creates a small modal popup window.
        // Modal means the admin must close this popup before using the main window again.
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
        // Deletes from the correct manager depending on which table is open.
        // This keeps database access inside BLL/DAL instead of inside the GUI.
        boolean deleted = switch (currentPage) {
            case ATTENDANCE -> false;
            case USERS -> userManager.deactivateUser(((User) selectedRow).getId());
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
        // Reloads the table that is currently visible.
        // This is used after Add/Edit/Delete and by the refresh button.
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

    private void setTableRows(List<?> rows) {
        // Saves the unfiltered rows first.
        // Search uses this list so it can always go back to all rows.
        currentRows = new ArrayList<>(rows);
        applySearchFilter();
    }

    private void applySearchFilter() {
        String searchText = txtSearch.getText();

        if (searchText == null || searchText.isBlank()) {
            tableAdmin.setItems(FXCollections.observableArrayList(currentRows));
            return;
        }

        String search = searchText.toLowerCase(Locale.ROOT);
        List<Object> filteredRows = currentRows.stream()
                .filter(row -> rowMatchesSearch(row, search))
                .toList();

        tableAdmin.setItems(FXCollections.observableArrayList(filteredRows));
    }

    private boolean rowMatchesSearch(Object row, String search) {
        return switch (currentPage) {
            case ATTENDANCE, USERS -> userMatchesSearch((User) row, search);
            case PROFILES -> profileMatchesSearch((Profile) row, search);
            case CLIENTS -> clientMatchesSearch((Client) row, search);
        };
    }

    private boolean userMatchesSearch(User user, String search) {
        return containsSearch(user.getUsername(), search)
                || containsSearch(user.getRoleName(), search)
                || containsSearch(user.isActive() ? "Yes" : "No", search)
                || containsSearch(isLoggedInNow(user) ? "Yes" : "No", search)
                || containsSearch(formatDateTime(user.getLastLogin()), search);
    }

    private boolean profileMatchesSearch(Profile profile, String search) {
        return containsSearch(profile.getName(), search)
                || containsSearch(profile.getClientName(), search)
                || containsSearch(formatDate(profile.getCreatedAt()), search);
    }

    private boolean clientMatchesSearch(Client client, String search) {
        return containsSearch(client.getName(), search)
                || containsSearch(formatDate(client.getCreatedAt()), search);
    }

    private boolean containsSearch(String value, String search) {
        return safeText(value).toLowerCase(Locale.ROOT).contains(search);
    }

    private TableColumn<Object, String> textColumn(String title, TextGetter getter) {
        // Small helper for creating text columns.
        // The TextGetter decides what text each row should show in this column.
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
        // The current user is always logged in.
        // Other users count as logged in if their LastLogin is less than 30 minutes ago.
        if (Session.getUser() != null && Session.getUser().getId().equals(user.getId())) {
            return true;
        }

        if (user.getLastLogin() == null) {
            return false;
        }

        return Duration.between(user.getLastLogin(), LocalDateTime.now()).toMinutes() < 30;
    }

    private void setActiveButton(Button activeButton) {
        btnAttendance.getStyleClass().removeAll("nav-btn-active");
        btnUsers.getStyleClass().removeAll("nav-btn-active");
        btnProfiles.getStyleClass().removeAll("nav-btn-active");
        btnClients.getStyleClass().removeAll("nav-btn-active");

        btnAttendance.setStyle("");
        btnUsers.setStyle("");
        btnProfiles.setStyle("");
        btnClients.setStyle("");

        activeButton.getStyleClass().add("nav-btn-active");
        activeButton.setStyle(ACTIVE_NAV_STYLE);
    }

    @FunctionalInterface
    private interface TextGetter {
        // This interface is used by textColumn().
        // It lets each table column decide how to get text from a row object.
        String getText(Object row);
    }

    private enum AdminPage {
        // Enum means a fixed list of possible admin pages.
        // It is safer than using plain text like "USERS" because Java catches spelling mistakes.
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
