package de.bayerl.statistics.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Class that redirects menu interaction to the mainapp
 */
public class MenuBarController {

    private MainApp mainApp;

    /**
     * Set the mainapp for this controller
     *
     * @param mainApp desired mainapp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Opens a file chooser where the user can select his tables and redirects the path to the mainapp
     */
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

    /**
     * Opens a filechooser where the user can load a list of transformations and redirects the path to the mainapp
     */
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

    /**
     * Opens a filechooser where the user can save a list of transformations and redirects the path to the mainapp
     */
    @FXML
    private void saveFile() {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // Make sure file has the correct extension
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            mainApp.saveTransformations(file);
        }
    }
}
