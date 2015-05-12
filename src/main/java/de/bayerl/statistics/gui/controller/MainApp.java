package de.bayerl.statistics.gui.controller;
import de.bayerl.statistics.gui.model.ListWrapper;
import de.bayerl.statistics.gui.model.Parameter;
import de.bayerl.statistics.gui.model.TransformationModel;
import de.bayerl.statistics.model.Table;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
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
    private List<File> tables;
    private Table transformationTable;
    private String htmlFolder = "";
    private MainViewController mainViewController;
    private List<String> correspondingFileNames;

	@Override
	public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Statistics2Cubes");

        correspondingFileNames = new ArrayList<>();
        initRootLayout();
        showMainView();
	}

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        tables = new ArrayList<>();
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
            mainViewController = loader.getController();
            mainViewController.setMainApp(this);

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
        tables.add(file);
    }

    public void load() {
        if(htmlFolder.equals("")) {
            String path = System.getProperty("user.home");
            path += File.separator + "htmlTables";
            File customDir = new File(path);

            if (customDir.exists()) {
                System.out.println(customDir + " already exists");
            } else if (customDir.mkdirs()) {
                System.out.println(customDir + " was created");
            } else {
                System.out.println(customDir + " was not created");
            }
            htmlFolder = customDir.getAbsolutePath();
        }
        transformationTable = Handler.load(tables, htmlFolder);
    }

    public void transform() {
        correspondingFileNames.addAll(Handler.transform(transformationTable, transformations, htmlFolder));
        updateWebView(correspondingFileNames.get(correspondingFileNames.size()-1));
    }

    public String getInbetween(String toEdit, String start, String end) {
        int startLength = start.length();
        int endLength = end.length();
        int startPos = 0;
        for(int i = 0; i < toEdit.length(); i++) {
            if(toEdit.charAt(i) == '<') {
                startPos = i;
                break;
            }
        }

        StringBuilder builder = new StringBuilder();
        for(int i = startPos + startLength; i < toEdit.length() - endLength; i++) {
            builder.append(toEdit.charAt(i));
        }
        return builder.toString();
    }

    public void loadTransformations(File file) {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            List<TransformationModel> models = new ArrayList<>();
            TransformationModel model = null;
            String currentElement = "";
            List<Integer> intList = new ArrayList<>();
            List<String> stringList = new ArrayList<>();
            while (line != null) {
                if(line.replaceAll(" ", "").startsWith("<transformation>")) {
                    model = new TransformationModel("", new ArrayList<>());
                } else if(line.contains("<intList>")) {
                    currentElement = "intList";
                    intList.add(Integer.parseInt(getInbetween(line, "<intList>", "</intList>")));
                }else if(line.contains("<stringList>")) {
                    currentElement = "stringList";
                    stringList.add(getInbetween(line, "<stringList>", "</stringList>"));
                } else if(line.contains("<value>")) {
                    currentElement = "value";
                    model.getAttributes().add(new Parameter(getInbetween(line, "<value>", "</value>")));
                } else if(line.contains("</attributes>")) {
                    if(currentElement.equals("intList")) {
                        model.getAttributes().add(new Parameter(intList));
                        intList = new ArrayList<>();
                    } else if(currentElement.equals("stringList")) {
                        model.getAttributes().add(new Parameter(stringList));
                        stringList = new ArrayList<>();
                    }
                } else if(line.contains("<name>")) {
                    model.setName(getInbetween(line.replaceAll(" ", ""), "<name>", "</name>"));
                    models.add(model);
                }
                line = br.readLine();
            }
              transformations.clear();
              transformations.addAll(models);
              System.out.println("Transformation list loaded successfully");
        } catch (Exception e) { // catches ANY exception
            e.printStackTrace();
            /*Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();*/
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
            primaryStage.setTitle("Transformator - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Update the stage title.
            primaryStage.setTitle("Transformator");
        }
    }

    public void updateWebView(String fileName) {
        mainViewController.updateWebView(fileName);
    }

    public ObservableList<TransformationModel> getTransformations() {
        return transformations;
    }

    public List<File> getTables() {
        return tables;
    }

    public void setTables(List<File> tables) {
        this.tables = tables;
    }

    public Table getTransformationTable() {
        return transformationTable;
    }

    public void setTransformationTable(Table transformationTable) {
        this.transformationTable = transformationTable;
    }

    public String getHtmlFolder() {
        return htmlFolder;
    }

    public void setHtmlFolder(String htmlFolder) {
        this.htmlFolder = htmlFolder;
    }

    public List<String> getCorrespondingFileNames() {
        return correspondingFileNames;
    }

    public void setCorrespondingFileNames(List<String> correspondingFileNames) {
        this.correspondingFileNames = correspondingFileNames;
    }
}
