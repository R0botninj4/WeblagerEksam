package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import com.eksam.weblagereksam.BLL.Manager.ProfileManager;
import com.eksam.weblagereksam.BLL.Manager.UserManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;

public class AdminController {

    @FXML private Button btnUsers, btnProfiles, btnClients;
    @FXML private TextField txtSearch;
    @FXML private TableView<Object> tableAdmin;

    private UserManager userManager;
    private ProfileManager profileManager;
    private ClientManager clientManager;

    @FXML
    public void initialize() {
        try {
            userManager = new UserManager();
            profileManager = new ProfileManager();
            clientManager = new ClientManager();

            showUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUsers() {
        txtSearch.setPromptText("Search user");
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
        setActiveButton(btnClients);

        tableAdmin.getColumns().setAll(
                textColumn("Name", row -> ((Client) row).getName()),
                textColumn("Code", row -> ((Client) row).getCode()),
                textColumn("Created", row -> formatDate(((Client) row).getCreatedAt()))
        );

        tableAdmin.setItems(FXCollections.observableArrayList(clientManager.getAllClients()));
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

    private void setActiveButton(Button activeButton) {
        btnUsers.getStyleClass().remove("nav-btn-active");
        btnProfiles.getStyleClass().remove("nav-btn-active");
        btnClients.getStyleClass().remove("nav-btn-active");
        activeButton.getStyleClass().add("nav-btn-active");
    }

    @FunctionalInterface
    private interface TextGetter {
        String getText(Object row);
    }
}
