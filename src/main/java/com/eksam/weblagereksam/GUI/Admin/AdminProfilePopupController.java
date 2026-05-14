package com.eksam.weblagereksam.GUI.Admin;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import com.eksam.weblagereksam.BLL.Manager.ProfileManager;
import com.eksam.weblagereksam.GUI.Util.ErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

public class AdminProfilePopupController implements AdminPopupController {

    @FXML private Label labelTitle;
    @FXML private TextField txtName;
    @FXML private ComboBox<Client> comboClient;

    private ClientManager clientManager;
    private ProfileManager profileManager;
    private Profile profile;
    private String action;
    private boolean saved;

    @FXML
    public void initialize() throws Exception {
        clientManager = new ClientManager();
        profileManager = new ProfileManager();
        comboClient.getItems().setAll(clientManager.getActiveClients());
    }

    public void setup(String action, Object selectedRow) {
        this.action = action;
        labelTitle.setText(action + " Profile");

        if (selectedRow instanceof Profile selectedProfile) {
            profile = selectedProfile;
            txtName.setText(profile.getName());
            comboClient.getSelectionModel().select(
                    comboClient.getItems().stream()
                            .filter(client -> client.getId().equals(profile.getClientId()))
                            .findFirst()
                            .orElse(null)
            );
        }
    }

    @FXML
    private void save() {
        try {
            Client selectedClient = comboClient.getSelectionModel().getSelectedItem();
            String name = txtName.getText();

            if (selectedClient == null || name == null || name.isBlank()) {
                ErrorDialog.show(txtName.getScene().getWindow(), "Select a client and write a profile name.", null);
                return;
            }

            if ("Edit".equals(action) && profile != null) {
                profile.setClientId(selectedClient.getId());
                profile.setName(name);
                saved = profileManager.updateProfile(profile);
            } else {
                UUID profileId = profileManager.createProfile(
                        selectedClient.getId(),
                        name,
                        null,
                        null
                );
                saved = profileId != null;
            }

            if (saved) {
                close();
            } else {
                ErrorDialog.show(txtName.getScene().getWindow(), "Profile could not be saved.", null);
            }
        } catch (Exception e) {
            ErrorDialog.show(txtName.getScene().getWindow(), "Profile could not be saved.", e);
        }
    }

    private void close() {
        ((Stage) txtName.getScene().getWindow()).close();
    }

    public boolean wasSaved() {
        return saved;
    }
}
