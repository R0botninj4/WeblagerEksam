package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BLL.Manager.BoxManager;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminClientPopupController implements AdminPopupController {

    @FXML private Label labelTitle;
    @FXML private TextField txtName, txtBoxNumber;
    @FXML private VBox boxListContainer;

    private ClientManager clientManager;
    private BoxManager boxManager;
    private Client client;
    private String action;
    private boolean saved;
    private final List<String> boxNumbers = new ArrayList<>();

    @FXML
    public void initialize() throws Exception {
        clientManager = new ClientManager();
        boxManager = new BoxManager();
    }

    public void setup(String action, Object selectedRow) {
        this.action = action;
        labelTitle.setText(action + " Client");

        if (selectedRow instanceof Client selectedClient) {
            client = selectedClient;
            txtName.setText(client.getName());

            for (Box box : boxManager.getBoxesByClientId(client.getId())) {
                boxListContainer.getChildren().add(new Label(box.getBoxNumber()));
            }
        }
    }

    @FXML
    private void addBoxName() {
        String boxNumber = txtBoxNumber.getText();

        if (boxNumber == null || boxNumber.isBlank()) {
            return;
        }

        boxNumbers.add(boxNumber);
        boxListContainer.getChildren().add(new Label(boxNumber));
        txtBoxNumber.clear();
    }

    @FXML
    private void save() {
        String name = txtName.getText();

        if (name == null || name.isBlank()) {
            return;
        }

        if ("Edit".equals(action) && client != null) {
            client.setName(name);
            saved = clientManager.updateClient(client);
            if (saved) {
                createBoxes(client.getId(), name);
            }
        } else {
            UUID clientId = clientManager.createClient(name, null);
            saved = clientId != null;
            if (saved) {
                createBoxes(clientId, name);
            }
        }

        if (saved) {
            close();
        }
    }

    private void createBoxes(UUID clientId, String clientName) {
        for (String boxNumber : boxNumbers) {
            Box box = new Box(
                    null,
                    clientId,
                    null,
                    clientName,
                    null,
                    boxNumber,
                    null,
                    "READY",
                    null
            );

            boxManager.createBox(box);
        }
    }

    private void close() {
        ((Stage) txtName.getScene().getWindow()).close();
    }

    public boolean wasSaved() {
        return saved;
    }
}
