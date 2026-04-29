package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Role;
import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.UserManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminUserPopupController implements AdminPopupController {

    @FXML private Label labelTitle;
    @FXML private TextField txtFullName, txtUsername, txtPassword;
    @FXML private ComboBox<Role> comboRole;

    private UserManager userManager;
    private User user;
    private String action;
    private boolean saved;

    @FXML
    public void initialize() throws Exception {
        userManager = new UserManager();
        comboRole.getItems().setAll(userManager.getAllRoles());
    }

    public void setup(String action, Object selectedRow) {
        this.action = action;
        labelTitle.setText(action + " User");

        if (selectedRow instanceof User selectedUser) {
            user = selectedUser;
            txtFullName.setText(user.getFullName());
            txtUsername.setText(user.getUsername());
            txtPassword.setPromptText("Leave empty to keep password");
            comboRole.getSelectionModel().select(
                    comboRole.getItems().stream()
                            .filter(role -> role.getId().equals(user.getRoleId()))
                            .findFirst()
                            .orElse(null)
            );
        }
    }

    @FXML
    private void save() {
        Role selectedRole = comboRole.getSelectionModel().getSelectedItem();

        if (selectedRole == null || txtUsername.getText().isBlank()) {
            return;
        }

        if ("Edit".equals(action) && user != null) {
            user.setUsername(txtUsername.getText());
            user.setFullName(txtFullName.getText());
            user.setRoleId(selectedRole.getId());
            saved = userManager.updateUser(user);
        } else {
            if (txtPassword.getText().isBlank()) {
                return;
            }

            saved = userManager.createUser(
                    txtUsername.getText(),
                    txtPassword.getText(),
                    txtFullName.getText(),
                    selectedRole.getId()
            ) != null;
        }

        if (saved) {
            close();
        }
    }

    private void close() {
        ((Stage) txtUsername.getScene().getWindow()).close();
    }

    public boolean wasSaved() {
        return saved;
    }
}
