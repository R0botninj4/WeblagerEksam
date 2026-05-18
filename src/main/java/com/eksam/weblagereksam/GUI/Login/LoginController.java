package com.eksam.weblagereksam.GUI.Login;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.LogManager;
import com.eksam.weblagereksam.BLL.Manager.UserManager;
import com.eksam.weblagereksam.GUI.Util.ErrorDialog;
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
    private LogManager logManager;

    // ===== JavaFX lifecycle =====

    @FXML
    public void initialize() {

        try {
            userManager = new UserManager();
            logManager = new LogManager();
        } catch (Exception e) {
            lblMessage.setText("Database fejl.");
            showException("Could not connect to the database.", e);
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
            logManager.createLog(user.getId(), "Login", "Users", user.getId(), null, user.getUsername());

            String viewPath;
            String windowTitle;

            if (user.getRoleName().equalsIgnoreCase("User")) {
                viewPath = "/com/eksam/weblagereksam/User-Scanning-view.fxml";
                windowTitle = "Weblager Scan";
            } else if (user.getRoleName().equalsIgnoreCase("Admin")) {
                viewPath = "/com/eksam/weblagereksam/Admin-view.fxml";
                windowTitle = "Weblager Admin";
            } else {
                viewPath = "/com/eksam/weblagereksam/Login-view.fxml";
                windowTitle = "Weblager Login";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle(windowTitle);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            Stage loginStage = (Stage) txtUsername.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            showError("Login fejl.");
            showException("Login failed.", e);
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

    private void showException(String message, Throwable error) {
        Stage owner = txtUsername != null && txtUsername.getScene() != null
                ? (Stage) txtUsername.getScene().getWindow()
                : null;
        ErrorDialog.show(owner, message, error);
    }
}
