package de.bayerl.statistics.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Created by Alexander on 01.05.2015.
 */
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
            for (File file : list) {
                mainApp.openTables(file);
            }
            mainApp.load();
        }
        mainApp.updateWebView("table_0_original.html");
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
