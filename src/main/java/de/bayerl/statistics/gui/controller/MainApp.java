package de.bayerl.statistics.gui.controller;
import de.bayerl.statistics.gui.model.ListWrapper;
import de.bayerl.statistics.gui.model.TransformationModel;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    final Desktop desktop = Desktop.getDesktop();
    private ObservableList<TransformationModel> transformations = FXCollections.observableArrayList();

	@Override
	public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Statistics2Cubes");

        initRootLayout();
        showMainView();
	}

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {

            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            MenuBarController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMainView() {
        try {
            // Load mainview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/MainView.fxml"));
            AnchorPane mainView = (AnchorPane) loader.load();

            // Set mainview into the center of root layout.
            rootLayout.setCenter(mainView);

            // Give the controller access to the main app.
            MainViewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
		launch(args);
	}

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void openTables(File file) {

    }

    public String getInbetween(String toEdit, String start, String end) {
        int startLength = start.length();
        int endLength = end.length();

        StringBuilder builder = new StringBuilder();
        for(int i = startLength; i < toEdit.length() - endLength; i++) {
            builder.append(toEdit.charAt(i));
        }
        return builder.toString();
    }

    public void loadTransformations(File file) {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            List<TransformationModel> models = new ArrayList<TransformationModel>();
            TransformationModel model = null;
            while (line != null) {
                if(line.contains("<transformation>")) {
                    model = new TransformationModel("", new ArrayList<String>());
                } else if(line.contains("<attributes>")) {
                    model.getAttributes().add(getInbetween(line.replaceAll(" ", ""), "<attributes>", "</attributes>"));
                } else if(line.contains("<name>")) {
                    model.setName(getInbetween(line.replaceAll(" ", ""), "<name>", "</name>"));
                    models.add(model);
                }
                line = br.readLine();
            }
            transformations.clear();
            transformations.addAll(models);
        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    public void saveTransformations(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(ListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping our person data.
            ListWrapper wrapper = new ListWrapper();
            wrapper.setTransformations(transformations);

            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);

            // Save the file path to the registry.
        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    public File getFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    public void setFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Update the stage title.
            primaryStage.setTitle("AddressApp - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Update the stage title.
            primaryStage.setTitle("AddressApp");
        }
    }

    public ObservableList<TransformationModel> getTransformations() {
        return transformations;
    }
}
