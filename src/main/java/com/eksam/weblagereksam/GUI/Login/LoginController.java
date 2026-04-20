package com.eksam.weblagereksam.GUI.Login;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
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
    @FXML
    private void initialize() {
        txtUsername.setOnAction(e -> handleLogin(new ActionEvent()));
        txtPassword.setOnAction(e -> handleLogin(new ActionEvent()));
    }
    private final UserManager userManager = new UserManager();

    @FXML
    private void handleLogin(ActionEvent actionEvent) {
        try {
            String username = txtUsername.getText();
            String password = txtPassword.getText();

            User user = userManager.login(username, password);

            if (user == null) {
                lblMessage.setText("Forkert brugernavn eller password");
                lblMessage.setStyle("-fx-text-fill: red;");
                return;
            }

            // GEM USER I SESSION
            Session.setUser(user);

            // ÅBN MAIN
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/eksam/weblagereksam/Main-view.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Event Manager");
            stage.setScene(scene);
            stage.show();

            // LUK LOGIN
            ((Stage) txtUsername.getScene().getWindow()).close();

        } catch (Exception e) {
            lblMessage.setText("Fejl: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public class PasswordHasher {

        private static final Argon2 argon2 = Argon2Factory.create();

        public static String hash(String password) {
            return argon2.hash(3, 65536, 1, password.toCharArray());
        }

        public static boolean verify(String hash, String password) {
            return argon2.verify(hash, password.toCharArray());
        }
    }
}
