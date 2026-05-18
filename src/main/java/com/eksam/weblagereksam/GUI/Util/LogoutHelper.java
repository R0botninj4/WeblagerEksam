package com.eksam.weblagereksam.GUI.Util;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BLL.Manager.LogManager;
import com.eksam.weblagereksam.GUI.Login.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class LogoutHelper {

    public void logout(Window currentWindow) {
        try {
            User user = Session.getUser();
            if (user != null) {
                new LogManager().createLog(user.getId(), "Logout", "Users", user.getId(), null, user.getUsername());
            }

            Session.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eksam/weblagereksam/Login-view.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(loader.load()));
            loginStage.show();

            if (currentWindow instanceof Stage currentStage) {
                currentStage.close();
            }
        } catch (Exception e) {
            ErrorDialog.show(currentWindow, "Logout failed.", e);
        }
    }
}
