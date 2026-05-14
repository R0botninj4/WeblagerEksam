package com.eksam.weblagereksam.GUI.Util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Window;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialog {

    public static void show(Window owner, String message, Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(error == null ? "No technical details available." : getShortMessage(error));

        if (owner != null) {
            alert.initOwner(owner);
        }

        if (error != null) {
            TextArea details = new TextArea(getStackTrace(error));
            details.setEditable(false);
            details.setWrapText(false);
            details.setPrefWidth(650);
            details.setPrefHeight(250);
            alert.getDialogPane().setExpandableContent(details);
        }

        alert.showAndWait();
    }

    public static void show(String message, Throwable error) {
        show(null, message, error);
    }

    private static String getShortMessage(Throwable error) {
        return error.getMessage() == null || error.getMessage().isBlank()
                ? error.getClass().getSimpleName()
                : error.getMessage();
    }

    private static String getStackTrace(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
