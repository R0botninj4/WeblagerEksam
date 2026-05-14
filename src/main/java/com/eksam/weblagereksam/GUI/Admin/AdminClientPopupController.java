package com.eksam.weblagereksam.GUI.Admin;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.BLL.Manager.ClientManager;
import com.eksam.weblagereksam.GUI.Util.ErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

public class AdminClientPopupController implements AdminPopupController {

    @FXML private Label labelTitle;
    @FXML private TextField txtName;

    private ClientManager clientManager;
    private Client client;
    private String action;
    private boolean saved;

    @FXML
    public void initialize() throws Exception {
        clientManager = new ClientManager();
    }

    public void setup(String action, Object selectedRow) {
        this.action = action;
        labelTitle.setText(action + " Client");

        if (selectedRow instanceof Client selectedClient) {
            client = selectedClient;
            txtName.setText(client.getName());
        }
    }

    @FXML
    private void save() {
        try {
            String name = txtName.getText();

            if (name == null || name.isBlank()) {
                ErrorDialog.show(txtName.getScene().getWindow(), "Write a client name.", null);
                return;
            }

            if ("Edit".equals(action) && client != null) {
                client.setName(name);
                saved = clientManager.updateClient(client);
            } else {
                UUID clientId = clientManager.createClient(name);
                saved = clientId != null;
            }

            if (saved) {
                close();
            } else {
                ErrorDialog.show(txtName.getScene().getWindow(), "Client could not be saved.", null);
            }
        } catch (Exception e) {
            ErrorDialog.show(txtName.getScene().getWindow(), "Client could not be saved.", e);
        }
    }

    private void close() {
        ((Stage) txtName.getScene().getWindow()).close();
    }

    public boolean wasSaved() {
        return saved;
    }
}
