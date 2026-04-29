package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.BLL.Manager.BoxManager;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import com.eksam.weblagereksam.BLL.Manager.ProfileManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminBoxPopupController implements AdminPopupController {

    @FXML private Label labelTitle;
    @FXML private TextField txtBoxNumber, txtLabel;
    @FXML private ComboBox<Client> comboClient;
    @FXML private ComboBox<Profile> comboProfile;
    @FXML private ComboBox<String> comboStatus;

    private BoxManager boxManager;
    private Box box;
    private String action;
    private boolean saved;

    @FXML
    public void initialize() throws Exception {
        boxManager = new BoxManager();
        ClientManager clientManager = new ClientManager();
        ProfileManager profileManager = new ProfileManager();

        comboClient.getItems().setAll(clientManager.getAllClients());
        comboProfile.getItems().setAll(profileManager.getAllProfiles());
        comboStatus.getItems().setAll("READY", "IN_PROGRESS", "WAITING_FOR_QA", "COMPLETED", "ARCHIVED");
        comboStatus.getSelectionModel().select("READY");
    }

    public void setup(String action, Object selectedRow) {
        this.action = action;
        labelTitle.setText(action + " Box");

        if (selectedRow instanceof Box selectedBox) {
            box = selectedBox;
            txtBoxNumber.setText(box.getBoxNumber());
            txtLabel.setText(box.getLabel());
            comboStatus.getSelectionModel().select(box.getStatus());

            comboClient.getSelectionModel().select(
                    comboClient.getItems().stream()
                            .filter(client -> client.getId().equals(box.getClientId()))
                            .findFirst()
                            .orElse(null)
            );

            comboProfile.getSelectionModel().select(
                    comboProfile.getItems().stream()
                            .filter(profile -> profile.getId().equals(box.getProfileId()))
                            .findFirst()
                            .orElse(null)
            );
        }
    }

    @FXML
    private void save() {
        Client selectedClient = comboClient.getSelectionModel().getSelectedItem();
        Profile selectedProfile = comboProfile.getSelectionModel().getSelectedItem();
        String boxNumber = txtBoxNumber.getText();

        if (selectedClient == null || boxNumber == null || boxNumber.isBlank()) {
            return;
        }

        if ("Edit".equals(action) && box != null) {
            box.setClientId(selectedClient.getId());
            box.setProfileId(selectedProfile != null ? selectedProfile.getId() : null);
            box.setClientName(selectedClient.getName());
            box.setProfileName(selectedProfile != null ? selectedProfile.getName() : null);
            box.setBoxNumber(boxNumber);
            box.setLabel(txtLabel.getText());
            box.setStatus(comboStatus.getSelectionModel().getSelectedItem());
            saved = boxManager.updateBox(box);
        } else {
            Box newBox = new Box(
                    null,
                    selectedClient.getId(),
                    selectedProfile != null ? selectedProfile.getId() : null,
                    selectedClient.getName(),
                    selectedProfile != null ? selectedProfile.getName() : null,
                    boxNumber,
                    txtLabel.getText(),
                    comboStatus.getSelectionModel().getSelectedItem(),
                    null
            );

            saved = boxManager.createBox(newBox) != null;
        }

        if (saved) {
            close();
        }
    }

    private void close() {
        ((Stage) txtBoxNumber.getScene().getWindow()).close();
    }

    public boolean wasSaved() {
        return saved;
    }
}
