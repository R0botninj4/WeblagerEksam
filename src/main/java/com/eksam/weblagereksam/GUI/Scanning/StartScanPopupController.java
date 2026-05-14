package com.eksam.weblagereksam.GUI.Scanning;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BE.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class StartScanPopupController {

    @FXML private TextField txtBoxNumber;
    @FXML private ComboBox<Client> comboClient;
    @FXML private ComboBox<Profile> comboProfile;
    @FXML private Label labelError;

    private final List<Profile> profiles = new ArrayList<>();
    private boolean started;

    public void setup(List<Client> clients, List<Profile> profiles) {
        this.profiles.clear();
        this.profiles.addAll(profiles);
        comboClient.getItems().setAll(clients);
        comboClient.valueProperty().addListener((obs, oldClient, newClient) -> showProfilesForClient(newClient));
    }

    @FXML
    private void startScan() {
        if (getBoxNumber().isBlank()) {
            labelError.setText("Write a box number.");
            return;
        }
        if (getSelectedProfile() == null) {
            labelError.setText("Select a client and profile.");
            return;
        }

        started = true;
        ((Stage) txtBoxNumber.getScene().getWindow()).close();
    }

    public boolean wasStarted() {
        return started;
    }

    public String getBoxNumber() {
        return txtBoxNumber.getText() == null ? "" : txtBoxNumber.getText().trim();
    }

    public Profile getSelectedProfile() {
        return comboProfile.getSelectionModel().getSelectedItem();
    }

    private void showProfilesForClient(Client client) {
        comboProfile.getItems().clear();
        comboProfile.getSelectionModel().clearSelection();

        if (client == null) {
            return;
        }

        comboProfile.getItems().setAll(profiles.stream()
                .filter(profile -> client.getId().equals(profile.getClientId()))
                .toList());
    }
}
