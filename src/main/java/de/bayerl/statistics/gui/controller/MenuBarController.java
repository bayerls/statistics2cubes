package de.bayerl.statistics.gui.controller;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class MenuBarController {

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void openChooser() {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "TEI files (*.tei)", "*.tei");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        List<File> list = fileChooser.showOpenMultipleDialog(mainApp.getPrimaryStage());
        if (list != null) {
            mainApp.openTables(list.get(0));
            mainApp.load();
        }
    }

    @FXML
    private void loadTransformations() {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            mainApp.loadTransformations(file);
        }
    }

    @FXML
    private void saveFile() {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // Make sure it has the correct extension
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            mainApp.saveTransformations(file);
        }
    }
}
