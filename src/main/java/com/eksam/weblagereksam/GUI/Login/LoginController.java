package com.eksam.weblagereksam.GUI.Login;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    // ===== FXML controls =====

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    private UserManager userManager;

    // ===== JavaFX lifecycle =====

    @FXML
    public void initialize() {

        try {
            userManager = new UserManager();
        } catch (Exception e) {
            lblMessage.setText("Database fejl.");
            e.printStackTrace();
        }

        txtUsername.setOnAction(e -> handleLogin());
        txtPassword.setOnAction(e -> handleLogin());
    }

    // ===== Login flow =====

    @FXML
    private void handleLogin() {

        try {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText();

            if (username.isBlank() || password.isBlank()) {
                showError("Indtast brugernavn og password.");
                return;
            }

            User user = userManager.login(username, password);

            if (user == null) {
                showError("Forkert brugernavn eller password.");
                return;
            }

            if (!user.isActive()) {
                showError("Brugeren er deaktiveret.");
                return;
            }

            Session.setUser(user);

            String viewPath;

            if (user.getRoleName().equalsIgnoreCase("User")) {
                viewPath = "/com/eksam/weblagereksam/User-Scanning-view.fxml";
            } else if (user.getRoleName().equalsIgnoreCase("Admin")) {
                viewPath = "/com/eksam/weblagereksam/Admin-view.fxml";
            } else {
                viewPath = "/com/eksam/weblagereksam/Login-view.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Diamond Nova");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            Stage loginStage = (Stage) txtUsername.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            showError("Login fejl.");
            e.printStackTrace();
        }
    }

    // ===== Test login shortcuts =====

    @FXML
    public void Bypass(ActionEvent event) {
        bypassLogin("admin", "GOD12");
    }

    @FXML
    public void BypassUser(ActionEvent event) {
        bypassLogin("user", "User");
    }

    @FXML
    public void BypassQA(ActionEvent event) {
        bypassLogin("QA", "QA");
    }

    private void bypassLogin(String username, String password) {
        txtUsername.setText(username);
        txtPassword.setText(password);
        handleLogin();
    }

    // ===== UI feedback =====

    private void showError(String text) {
        lblMessage.setText(text);
        lblMessage.setStyle("-fx-text-fill: red;");
    }
}
