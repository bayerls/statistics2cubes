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
import javafx.stage.Stage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Mainapplication that handles all user-interaction
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<TransformationModel> transformations = FXCollections.observableArrayList();
    private List<File> tables;
    //private Table transformationTable;
    private String htmlFolder = "";
    private String cubeFolder = "";
    private MainViewController mainViewController;
    private Table lastTransformation;
    private boolean metadata;
    private boolean headers;

    /**
     * list with filenames needed for reaction to clicks on transformationlist
     */
    private List<String> correspondingFileNames;

    /**
     * Starts the application and sets the primary stage aswell as the title
     *
     * @param primaryStage
     */
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
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            MenuBarController controller = loader.getController();
            controller.setMainApp(this);
            addObligatoryTransformations();

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the 3 obligatory transformations
     */
    private void addObligatoryTransformations() {
        transformations.add(new TransformationModel("ResolveLinebreaks", new ArrayList<>()));
        transformations.add(new TransformationModel("ResolveRowSpan", new ArrayList<>()));
        transformations.add(new TransformationModel("ResolveColSpan", new ArrayList<>()));
        correspondingFileNames.add(null);
        correspondingFileNames.add(null);
        correspondingFileNames.add(null);
    }

    /**
     * shows the mainview and binds it to its controllers
     */
    public void showMainView() {
        try {
            // Load mainview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/MainView.fxml"));
            AnchorPane mainView = loader.load();

            // Set mainview into the center of root layout.
            rootLayout.setCenter(mainView);

            // Give the controller access to the main app.
            mainViewController = loader.getController();
            mainViewController.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * start main program
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Generates the folders for html- and cube-files and creates the list of tables to be merged and transformed
     *
     * @param file file to be added to tables list
     */
    public void openTables(File file) {
        tables.add(file);
        String path = (new File(file.getParent())).getParent() + File.separator + "html";
        File customDir = new File(path);
        if (customDir.mkdirs()) {
            System.out.println(customDir + "created");
        }
        htmlFolder = customDir.getAbsolutePath();
        cubeFolder = (new File(htmlFolder).getParent()) + File.separator + "n3";
    }

    /**
     * Loads and merges the chosen tables and executes the 3 obligatory transformation on the merged table
     */
    @SuppressWarnings("unchecked")
    public void load() {
        //transformationTable = Handler.load(tables, htmlFolder);
        mainViewController.enableControls();
        checkTransformationList();

        // fetches first 3 obligatory transformations
        List<TransformationModel> models = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            models.add(transformations.get(i));
        }
        correspondingFileNames.clear();

        // execute obligatory transformaitons
        List<Object> list = Handler.transform(tables, models, htmlFolder);
        correspondingFileNames.addAll((List<String>) list.get(1));
        lastTransformation = (Table) list.get(0);
        for (int i = 3; i < transformations.size(); i++) {
            correspondingFileNames.add(null);
        }
        this.metadata = false;
        this.headers = false;
        updateWebView(new File(htmlFolder).listFiles()[3].getName());
    }

    /**
     * Checks if export is possible and triggers the export function of the Handler Class
     *
     * @param version export-version
     */
    public void export(String version) {
        // execute current transformations
        transform();
        if (metadata && headers) {
            Handler.export(lastTransformation, version, cubeFolder);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not export data");
            alert.setContentText("You need to create headers and add metadata");
            alert.showAndWait();
        }
    }

    /**
     * Checks current transformationlist for obligatory transformations
     */
    private void checkTransformationList() {
        boolean lB = false;
        boolean rS = false;
        boolean cS = false;
        for (int i = 0; i < 3 && i < transformations.size(); i++) {
            if (transformations.get(i).getName().equals("ResolveLinebreaks")) {
                lB = true;
            } else if (transformations.get(i).getName().equals("ResolveRowSpan")) {
                rS = true;
            } else if (transformations.get(i).getName().equals("ResolveColSpan")) {
                cS = true;
            }
        }

        // add missing transformations
        if (!lB) {
            transformations.add(0, new TransformationModel("ResolveLinebreaks", new ArrayList<>()));
            correspondingFileNames.add(null);
        }
        if (!rS) {
            transformations.add(0, new TransformationModel("ResolveRowSpan", new ArrayList<>()));
            correspondingFileNames.add(null);
        }
        if (!cS) {
            transformations.add(0, new TransformationModel("ResolveColSpan", new ArrayList<>()));
            correspondingFileNames.add(null);
        }
    }

    /**
     * Triggers transform function of Handler class and checks, if export is possible
     */
    @SuppressWarnings("unchecked")
    public void transform() {
        checkTransformationList();
        correspondingFileNames.clear();
        List<Object> list = Handler.transform(tables, transformations, htmlFolder);
        correspondingFileNames.addAll((List<String>) (list.get(1)));
        lastTransformation = (Table) (list.get(0));
        File[] dir = (new File(htmlFolder)).listFiles();

        // check if export is possible
        if (dir != null) {
            for (File file : dir) {

                // upate webview to last transformation
                if (file.getName().contains(correspondingFileNames.get(correspondingFileNames.size() - 1))) {
                    updateWebView(file.getName());
                }
                if (file.getName().contains("AddMetadata")) {
                    metadata = true;
                } else if (file.getName().contains("CreateHeaders")) {
                    headers = true;
                }
            }
        }
    }

    /**
     * Finds String between two Strings
     *
     * @param toEdit String to edit
     * @param start start tag
     * @param end end tag
     * @return String between start and end
     */
    public String between(String toEdit, String start, String end) {
        int startLength = start.length();
        int endLength = end.length();
        int startPos = 0;

        // find start position
        for (int i = 0; i < toEdit.length(); i++) {
            if (toEdit.charAt(i) == '<') {
                startPos = i;
                break;
            }
        }

        // build new string
        StringBuilder builder = new StringBuilder();
        for (int i = startPos + startLength; i < toEdit.length() - endLength; i++) {
            builder.append(toEdit.charAt(i));
        }
        return builder.toString();
    }

    /**
     * Fills the lists needed to react to transformation-adding and -execution with empty values
     *
     * @param models models to derive lists from
     */
    private void fillLists(List<TransformationModel> models) {
        transformations.clear();
        correspondingFileNames.clear();
        transformations.addAll(models);
        checkTransformationList();
        for (TransformationModel t : transformations) {
            correspondingFileNames.add(null);
        }
    }

    /**
     * Loads a list of transformations from the file system.
     *
     * @param file xml-file to load transformations from
     */
    public void loadTransformations(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            List<TransformationModel> models = new ArrayList<>();
            TransformationModel model = null;
            String currentElement = "";
            List<Integer> intList = new ArrayList<>();
            List<String> stringList = new ArrayList<>();
            while (line != null) {

                // add new TransformationModel if line starts with <transformation> tag
                if (line.replaceAll(" ", "").startsWith("<transformation>")) {
                    model = new TransformationModel("", new ArrayList<>());

                // fill int list with given integers if line contains <intList> tag
                } else if (line.contains("<intList>")) {
                    currentElement = "intList";
                    intList.add(Integer.parseInt(between(line, "<intList>", "</intList>")));

                // fill String list with given Strings if line contains <strngList> tag
                } else if (line.contains("<stringList>")) {
                    currentElement = "stringList";
                    stringList.add(between(line, "<stringList>", "</stringList>"));

                // add String value parameter to model attributes
                } else if (line.contains("<value>")) {
                    currentElement = "value";
                    if (model != null && model.getAttributes() != null) {
                        model.getAttributes().add(new Parameter(between(line, "<value>", "</value>")));
                    }

                // attributes tag closed
                } else if (line.contains("</attributes>")) {

                    // add int list to attributeList of model
                    if (currentElement.equals("intList")) {
                        if (model != null && model.getAttributes() != null) {
                            model.getAttributes().add(new Parameter(intList));
                        }
                        intList = new ArrayList<>();

                    // add string list to attributeList of model
                    } else if (currentElement.equals("stringList")) {
                        if (model != null && model.getAttributes() != null) {
                            model.getAttributes().add(new Parameter(stringList));
                        }
                        stringList = new ArrayList<>();
                    }

                // name the model
                } else if (line.contains("<name>")) {
                    if (model != null) {
                        model.setName(between(line.replaceAll(" ", ""), "<name>", "</name>"));
                    }
                    models.add(model);
                }
                line = br.readLine();
            }
            fillLists(models);
            System.out.println("Transformation list " + file.getName() + " loaded successfully");
        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());
            alert.showAndWait();
        }
    }

    /**
     * Saves current transformation-list into given file
     *
     * @param file xml file to save transformations to
     */
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

    /**
     * Get default filepath
     *
     * @return default filepath
     */
    public File getFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Set default filepath
     *
     * @param file default filepath
     */
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

    /**
     * Updates the Webview and shows given table
     *
     * @param fileName tabke to show
     */
    public void updateWebView(String fileName) {
        mainViewController.updateWebView(fileName);
    }

    /**
     *Getter for primaryStage
     *
     * @return primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Getter for transformations
     *
     * @return list of current transformations
     */
    public ObservableList<TransformationModel> getTransformations() {
        return transformations;
    }

    /**
     *  Getter for html filepath
     *
     * @return folder for htmls
     */
    public String getHtmlFolder() {
        return htmlFolder;
    }

    /**
     * Getter for correspondingFileNames
     *
     * @return list with filenames needed for reaction to clicks on transformationlist
     */
    public List<String> getCorrespondingFileNames() {
        return correspondingFileNames;
    }

}
