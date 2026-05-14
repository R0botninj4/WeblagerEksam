package com.eksam.weblagereksam.GUI.Scanning;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BLL.Manager.ExportManager.ExportFormat;
import com.eksam.weblagereksam.GUI.Util.BoxDisplayConverter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.util.List;

public class MyBoxesPopupController {

    public enum Action {
        NONE, OPEN_SAVED, DONE, REMOVE, EXPORT
    }

    @FXML private ComboBox<Box> comboSavedBoxes;
    @FXML private Label labelOutputName, labelFormat;
    @FXML private Button btnMultiPage, btnSinglePage, btnExport;

    private Action action = Action.NONE;
    private ExportFormat selectedFormat = ExportFormat.MULTI_PAGE;

    public void setup(List<Box> savedBoxes, Box currentBox, ExportFormat currentFormat, int documentCount) {
        selectedFormat = currentFormat;
        comboSavedBoxes.setConverter(new BoxDisplayConverter());
        comboSavedBoxes.getItems().setAll(savedBoxes);
        selectCurrentBox(currentBox);

        labelOutputName.setText(currentBox == null ? "No box" : currentBox.getBoxNumber());
        btnExport.setText("Export (" + documentCount + " documents)");
        updateFormatText();
        updateFormatButtons();
    }

    @FXML private void openSaved() { closeWith(Action.OPEN_SAVED); }
    @FXML private void markDone() { closeWith(Action.DONE); }
    @FXML private void removeBox() { closeWith(Action.REMOVE); }
    @FXML private void exportBox() { closeWith(Action.EXPORT); }

    @FXML
    private void useMultiPage() {
        selectedFormat = ExportFormat.MULTI_PAGE;
        updateFormatText();
        updateFormatButtons();
    }

    @FXML
    private void useSinglePage() {
        selectedFormat = ExportFormat.SINGLE_PAGE;
        updateFormatText();
        updateFormatButtons();
    }

    public Action getAction() {
        return action;
    }

    public Box getSelectedBox() {
        return comboSavedBoxes.getSelectionModel().getSelectedItem();
    }

    public ExportFormat getSelectedFormat() {
        return selectedFormat;
    }

    private void selectCurrentBox(Box currentBox) {
        if (currentBox == null) {
            return;
        }

        comboSavedBoxes.getItems().stream()
                .filter(box -> box.getId().equals(currentBox.getId()))
                .findFirst()
                .ifPresent(box -> comboSavedBoxes.getSelectionModel().select(box));
    }

    private void updateFormatText() {
        labelFormat.setText(selectedFormat == ExportFormat.MULTI_PAGE ? "TIFF Multi-page" : "TIFF Single-page");
    }

    private void updateFormatButtons() {
        btnMultiPage.getStyleClass().removeAll("btn-format-active");
        btnSinglePage.getStyleClass().removeAll("btn-format-active");

        if (selectedFormat == ExportFormat.MULTI_PAGE) {
            btnMultiPage.getStyleClass().add("btn-format-active");
        } else {
            btnSinglePage.getStyleClass().add("btn-format-active");
        }
    }

    private void closeWith(Action action) {
        this.action = action;
        ((Stage) comboSavedBoxes.getScene().getWindow()).close();
    }
}
