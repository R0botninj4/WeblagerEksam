package com.eksam.weblagereksam.GUI.Login;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    private UserManager userManager;

    @FXML
    public void initialize() {

        try {
            userManager = new UserManager();
        } catch (Exception e) {
            lblMessage.setText("Database fejl.");
            e.printStackTrace();
        }

        txtUsername.setOnAction(e -> handleLogin(new ActionEvent()));
        txtPassword.setOnAction(e -> handleLogin(new ActionEvent()));
    }

    @FXML
    private void handleLogin(ActionEvent event) {

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

            // save logged in user
            Session.setUser(user);

            // open main system
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/eksam/weblagereksam/Main-view.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Diamond Nova");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            // close login window
            Stage loginStage =
                    (Stage) txtUsername.getScene().getWindow();

            loginStage.close();

        } catch (Exception e) {
            showError("Login fejl.");
            e.printStackTrace();
        }
    }

    //------Bypass----------
    @FXML
    public void Bypass(ActionEvent event) {
        txtUsername.setText("admin");
        txtPassword.setText("GOD12");

        handleLogin(new ActionEvent());
    }
    @FXML
    public void BypassUser(ActionEvent event) {
        txtUsername.setText("user");
        txtPassword.setText("User");

        handleLogin(new ActionEvent());
    }
    @FXML
    public void BypassQA(ActionEvent event) {
        txtUsername.setText("QA");
        txtPassword.setText("QA");

        handleLogin(new ActionEvent());
    }

    //----------------------------------------------

    private void showError(String text) {
        lblMessage.setText(text);
        lblMessage.setStyle("-fx-text-fill: red;");
    }
}