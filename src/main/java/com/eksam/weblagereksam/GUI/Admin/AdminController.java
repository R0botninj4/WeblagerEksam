package com.eksam.weblagereksam.GUI.Admin;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BE.LogEntry;
import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BE.UserActivity;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import com.eksam.weblagereksam.BLL.Manager.LogManager;
import com.eksam.weblagereksam.BLL.Manager.ProfileManager;
import com.eksam.weblagereksam.BLL.Manager.UserManager;
import com.eksam.weblagereksam.GUI.Login.Session;
import com.eksam.weblagereksam.GUI.Util.ErrorDialog;
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
import java.util.UUID;
import java.util.function.Function;

public class AdminController {

    // ===== Styling =====

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
    @FXML private Button btnAttendance, btnUsers, btnProfiles, btnClients, btnLogged, btnActivate;
    @FXML private TextField txtSearch;
    @FXML private TableView<Object> tableAdmin;

    // ===== Managers and page state =====

    // Managers belong to the BLL layer.
    // The controller asks managers for data instead of talking directly to the database.
    private UserManager userManager;
    private ProfileManager profileManager;
    private ClientManager clientManager;
    private LogManager logManager;
    private LogoutHelper logoutHelper;
    private List<Object> currentRows = new ArrayList<>();

    // Keeps track of which admin page/table is currently shown.
    // Example: USERS means Add/Edit/Delete should open the user popup.
    private AdminPage currentPage = AdminPage.USERS;

    @FXML
    public void initialize() {
        try {
            userManager = new UserManager();
            profileManager = new ProfileManager();
            clientManager = new ClientManager();
            logManager = new LogManager();
            logoutHelper = new LogoutHelper();

            setupTableContextMenu();
            setupSearchBar();
            showUsers();
        } catch (Exception e) {
            showError("Admin page could not start.", e);
        }
    }

    // ===== Setup =====

    private void setupSearchBar() {
        // Every time the admin types in the search field, the current table is filtered.
        // The full list is kept in currentRows, so clearing search shows everything again.
        txtSearch.textProperty().addListener((obs, oldText, newText) -> applySearchFilter());
    }

    private void setupTableContextMenu() {
        // This is the menu shown when the admin right-clicks the table.
        // It reuses the same methods as the toolbar buttons, so the logic only exists once.
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem activateItem = new MenuItem("Activate");

        editItem.setOnAction(event -> handleEdit());
        deleteItem.setOnAction(event -> handleDelete());
        activateItem.setOnAction(event -> handleActivate());

        ContextMenu contextMenu = new ContextMenu(editItem, deleteItem, activateItem);
        contextMenu.setOnShowing(event -> {
            // Attendance is read-only, so the edit actions should not be clickable there.
            // Activate is only useful when the selected row is currently inactive.
            boolean readOnlyPage = currentPage == AdminPage.ATTENDANCE || currentPage == AdminPage.LOGS;
            boolean rowSelected = tableAdmin.getSelectionModel().getSelectedItem() != null;

            editItem.setDisable(readOnlyPage || !rowSelected);
            deleteItem.setDisable(readOnlyPage || !rowSelected);
            activateItem.setDisable(readOnlyPage || !selectedRowIsInactive());
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
                textColumn("Username", row -> ((UserActivity) row).getUser().getUsername()),
                textColumn("Logged in now", row -> isLoggedInNow(((UserActivity) row).getUser()) ? "Yes" : "No"),
                textColumn("Last login", row -> formatDateTime(((UserActivity) row).getUser().getLastLogin())),
                textColumn("Boxes", row -> String.valueOf(((UserActivity) row).getBoxCount())),
                textColumn("Docs", row -> String.valueOf(((UserActivity) row).getDocumentCount())),
                textColumn("Pages", row -> String.valueOf(((UserActivity) row).getPageCount()))
        );

        setTableRows(userManager.getUserActivities());
    }

    // ===== Page buttons =====

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
                textColumn("Active", row -> ((Profile) row).isActive() ? "Yes" : "No"),
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
                textColumn("Active", row -> ((Client) row).isActive() ? "Yes" : "No"),
                textColumn("Created", row -> formatDate(((Client) row).getCreatedAt()))
        );

        setTableRows(clientManager.getAllClients());
    }

    @FXML
    private void showLogs() {
        txtSearch.setPromptText("Search logs");
        currentPage = AdminPage.LOGS;
        setActiveButton(btnLogged);

        tableAdmin.getColumns().setAll(
                textColumn("Time", row -> formatDateTime(((LogEntry) row).getCreatedAt())),
                textColumn("User", row -> ((LogEntry) row).getUsername()),
                textColumn("Action", row -> ((LogEntry) row).getAction()),
                textColumn("Table", row -> ((LogEntry) row).getTableName()),
                textColumn("New value", row -> ((LogEntry) row).getNewValue())
        );

        setTableRows(logManager.getAllLogs());
    }

    @FXML
    private void handleAdd() {
        if (currentPage == AdminPage.ATTENDANCE || currentPage == AdminPage.LOGS) {
            showInfo(currentPage.singularName + " is only for viewing.");
            return;
        }

        openCurrentPagePopup("Add");
    }

    // ===== Add, edit and delete =====

    @FXML
    private void handleEdit() {
        if (currentPage == AdminPage.ATTENDANCE || currentPage == AdminPage.LOGS) {
            showInfo(currentPage.singularName + " is only for viewing.");
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
        if (currentPage == AdminPage.ATTENDANCE || currentPage == AdminPage.LOGS) {
            showInfo(currentPage.singularName + " is only for viewing.");
            return;
        }

        Object selectedRow = tableAdmin.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showInfo("Select a row before deleting.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        boolean userPage = currentPage == AdminPage.USERS;
        boolean clientPage = currentPage == AdminPage.CLIENTS;
        alert.setTitle((userPage || clientPage ? "Deactivate " : "Deactivate ") + currentPage.singularName);
        alert.setHeaderText("Deactivate selected " + currentPage.singularName.toLowerCase() + "?");
        alert.setContentText(clientPage
                ? "The client will stay in the database. All profiles for this client will also be deactivated."
                : "The " + currentPage.singularName.toLowerCase() + " will stay in the database, but will no longer be active.");

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
    private void handleActivate() {
        if (currentPage == AdminPage.ATTENDANCE) {
            showInfo("Attendance is only for viewing login status.");
            return;
        }

        Object selectedRow = tableAdmin.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showInfo("Select a row before activating.");
            return;
        }

        if (!selectedRowIsInactive()) {
            showInfo("This " + currentPage.singularName.toLowerCase() + " is already active.");
            return;
        }

        activateSelectedRow(selectedRow);
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
            showError("Could not open " + currentPage.singularName.toLowerCase() + " popup.", e);
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
            writeLog(action + " " + currentPage.singularName, currentPage.singularName, null, null, title);
            refreshCurrentPage();
        }
    }

    private void deleteSelectedRow(Object selectedRow) {
        // Deletes from the correct manager depending on which table is open.
        // This keeps database access inside BLL/DAL instead of inside the GUI.
        boolean deleted = switch (currentPage) {
            case ATTENDANCE, LOGS -> false;
            case USERS -> userManager.deactivateUser(((User) selectedRow).getId());
            case PROFILES -> profileManager.deactivateProfile(((Profile) selectedRow).getId());
            case CLIENTS -> clientManager.deactivateClient(((Client) selectedRow).getId());
        };

        if (deleted) {
            writeLog("Deactivate " + currentPage.singularName, currentPage.singularName, getRowId(selectedRow), null, getRowName(selectedRow));
            refreshCurrentPage();
        } else {
            showError("Could not deactivate selected " + currentPage.singularName.toLowerCase() + ".", null);
        }
    }

    private void activateSelectedRow(Object selectedRow) {
        // Reactivation is also handled through the managers.
        // The database row stays the same; only IsActive is changed back to true.
        boolean activated = switch (currentPage) {
            case ATTENDANCE, LOGS -> false;
            case USERS -> userManager.activateUser(((User) selectedRow).getId());
            case PROFILES -> profileManager.activateProfile(((Profile) selectedRow).getId());
            case CLIENTS -> clientManager.activateClient(((Client) selectedRow).getId());
        };

        if (activated) {
            writeLog("Activate " + currentPage.singularName, currentPage.singularName, getRowId(selectedRow), null, getRowName(selectedRow));
            refreshCurrentPage();
        } else {
            showError("Could not activate selected " + currentPage.singularName.toLowerCase() + ".", null);
        }
    }

    private void refreshCurrentPage() {
        // Reloads the table that is currently visible.
        // This is used after Add/Edit/Delete and by the refresh button.
        switch (currentPage) {
            case ATTENDANCE -> showAttendance();
            case LOGS -> showLogs();
            case USERS -> showUsers();
            case PROFILES -> showProfiles();
            case CLIENTS -> showClients();
        }
    }

    // ===== Search and table data =====

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Throwable error) {
        Window owner = tableAdmin != null && tableAdmin.getScene() != null ? tableAdmin.getScene().getWindow() : null;
        ErrorDialog.show(owner, message, error);
    }

    private void writeLog(String action, String tableName, UUID recordId, String oldValue, String newValue) {
        UUID userId = Session.getUser() == null ? null : Session.getUser().getId();
        logManager.createLog(userId, action, tableName, recordId, oldValue, newValue);
    }

    private UUID getRowId(Object row) {
        return switch (currentPage) {
            case USERS -> ((User) row).getId();
            case PROFILES -> ((Profile) row).getId();
            case CLIENTS -> ((Client) row).getId();
            case ATTENDANCE, LOGS -> null;
        };
    }

    private String getRowName(Object row) {
        return switch (currentPage) {
            case USERS -> ((User) row).getUsername();
            case PROFILES -> ((Profile) row).getName();
            case CLIENTS -> ((Client) row).getName();
            case ATTENDANCE, LOGS -> null;
        };
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
            case ATTENDANCE -> userActivityMatchesSearch((UserActivity) row, search);
            case LOGS -> logMatchesSearch((LogEntry) row, search);
            case USERS -> userMatchesSearch((User) row, search);
            case PROFILES -> profileMatchesSearch((Profile) row, search);
            case CLIENTS -> clientMatchesSearch((Client) row, search);
        };
    }

    private boolean logMatchesSearch(LogEntry log, String search) {
        return containsSearch(log.getUsername(), search)
                || containsSearch(log.getAction(), search)
                || containsSearch(log.getTableName(), search)
                || containsSearch(formatUuid(log.getRecordId()), search)
                || containsSearch(log.getOldValue(), search)
                || containsSearch(log.getNewValue(), search)
                || containsSearch(formatDateTime(log.getCreatedAt()), search);
    }

    private boolean userActivityMatchesSearch(UserActivity activity, String search) {
        User user = activity.getUser();

        return userMatchesSearch(user, search)
                || containsSearch(String.valueOf(activity.getBoxCount()), search)
                || containsSearch(String.valueOf(activity.getDocumentCount()), search)
                || containsSearch(String.valueOf(activity.getPageCount()), search);
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
                || containsSearch(profile.isActive() ? "Yes" : "No", search)
                || containsSearch(formatDate(profile.getCreatedAt()), search);
    }

    private boolean clientMatchesSearch(Client client, String search) {
        return containsSearch(client.getName(), search)
                || containsSearch(client.isActive() ? "Yes" : "No", search)
                || containsSearch(formatDate(client.getCreatedAt()), search);
    }

    private boolean selectedRowIsInactive() {
        Object selectedRow = tableAdmin.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            return false;
        }

        return switch (currentPage) {
            case ATTENDANCE -> false;
            case LOGS -> false;
            case USERS -> !((User) selectedRow).isActive();
            case PROFILES -> !((Profile) selectedRow).isActive();
            case CLIENTS -> !((Client) selectedRow).isActive();
        };
    }

    private boolean containsSearch(String value, String search) {
        return safeText(value).toLowerCase(Locale.ROOT).contains(search);
    }

    private TableColumn<Object, String> textColumn(String title, Function<Object, String> getter) {
        // Small helper for creating text columns.
        // The function decides what text each row should show in this column.
        TableColumn<Object, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(safeText(getter.apply(cell.getValue()))));
        return column;
    }

    // ===== Text and date helpers =====

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "-" : date.toLocalDate().toString();
    }

    private String formatDateTime(LocalDateTime date) {
        return date == null ? "Never" : date.toString().replace("T", " ");
    }

    private String formatUuid(UUID id) {
        return id == null ? "-" : id.toString();
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

    // ===== Navigation styling =====

    private void setActiveButton(Button activeButton) {
        for (Button button : navButtons()) {
            button.getStyleClass().removeAll("nav-btn-active");
            button.setStyle("");
        }

        activeButton.getStyleClass().add("nav-btn-active");
        activeButton.setStyle(ACTIVE_NAV_STYLE);
    }

    private List<Button> navButtons() {
        return List.of(btnAttendance, btnUsers, btnProfiles, btnClients, btnLogged);
    }

    // ===== Admin pages =====

    private enum AdminPage {
        // Enum means a fixed list of possible admin pages.
        // It is safer than using plain text like "USERS" because Java catches spelling mistakes.
        ATTENDANCE("Attendance", ""),
        LOGS("Logged activity", ""),
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
