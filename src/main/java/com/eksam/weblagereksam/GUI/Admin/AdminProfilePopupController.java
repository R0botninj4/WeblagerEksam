package com.eksam.weblagereksam.GUI.Admin;

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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminProfilePopupController implements AdminPopupController {

    @FXML private Label labelTitle;
    @FXML private TextField txtName;
    @FXML private ComboBox<Client> comboClient;
    @FXML private ComboBox<Box> comboBox;
    @FXML private VBox selectedBoxesContainer;

    private BoxManager boxManager;
    private ClientManager clientManager;
    private ProfileManager profileManager;
    private Profile profile;
    private String action;
    private boolean saved;
    private boolean settingUp;
    private final List<Box> selectedBoxes = new ArrayList<>();

    @FXML
    public void initialize() throws Exception {
        boxManager = new BoxManager();
        clientManager = new ClientManager();
        profileManager = new ProfileManager();
        comboClient.getItems().setAll(clientManager.getAllClients());
        comboClient.setOnAction(event -> loadBoxesForSelectedClient());
    }

    public void setup(String action, Object selectedRow) {
        this.action = action;
        labelTitle.setText(action + " Profile");

        if (selectedRow instanceof Profile selectedProfile) {
            settingUp = true;
            profile = selectedProfile;
            txtName.setText(profile.getName());
            comboClient.getSelectionModel().select(
                    comboClient.getItems().stream()
                            .filter(client -> client.getId().equals(profile.getClientId()))
                            .findFirst()
                            .orElse(null)
            );

            loadBoxesForSelectedClient();

            for (Box box : boxManager.getBoxesByProfileId(profile.getId())) {
                addBoxToSelectedList(box);
            }
            settingUp = false;
        }
    }

    @FXML
    private void addSelectedBox() {
        Box selectedBox = comboBox.getSelectionModel().getSelectedItem();

        if (selectedBox == null || boxAlreadySelected(selectedBox)) {
            return;
        }

        addBoxToSelectedList(selectedBox);
    }

    private void loadBoxesForSelectedClient() {
        Client selectedClient = comboClient.getSelectionModel().getSelectedItem();

        comboBox.getItems().clear();
        comboBox.getSelectionModel().clearSelection();

        if (!settingUp) {
            selectedBoxes.clear();
            selectedBoxesContainer.getChildren().clear();
        }

        if (selectedClient == null) {
            return;
        }

        comboBox.getItems().setAll(boxManager.getBoxesByClientId(selectedClient.getId()));
    }

    private void addBoxToSelectedList(Box box) {
        selectedBoxes.add(box);
        selectedBoxesContainer.getChildren().add(new Label(box.getBoxNumber()));
    }

    private boolean boxAlreadySelected(Box box) {
        return selectedBoxes.stream().anyMatch(selectedBox -> selectedBox.getId().equals(box.getId()));
    }

    @FXML
    private void save() {
        Client selectedClient = comboClient.getSelectionModel().getSelectedItem();
        String name = txtName.getText();

        if (selectedClient == null || name == null || name.isBlank()) {
            return;
        }

        if ("Edit".equals(action) && profile != null) {
            profile.setClientId(selectedClient.getId());
            profile.setName(name);
            saved = profileManager.updateProfile(profile);
            if (saved) {
                assignBoxesToProfile(profile.getId(), profile.getName());
            }
        } else {
            UUID profileId = profileManager.createProfile(
                    selectedClient.getId(),
                    name,
                    null,
                    null
            );
            saved = profileId != null;
            if (saved) {
                assignBoxesToProfile(profileId, name);
            }
        }

        if (saved) {
            close();
        }
    }

    private void assignBoxesToProfile(UUID profileId, String profileName) {
        for (Box box : selectedBoxes) {
            box.setProfileId(profileId);
            box.setProfileName(profileName);
            boxManager.updateBox(box);
        }
    }

    private void close() {
        ((Stage) txtName.getScene().getWindow()).close();
    }

    public boolean wasSaved() {
        return saved;
    }
}
