package de.bayerl.statistics.gui.controller;
import de.bayerl.statistics.gui.MainApp;
import de.bayerl.statistics.gui.model.storageStructure.Parameter;
import de.bayerl.statistics.gui.model.storageStructure.TransformationModel;
import de.bayerl.statistics.transformer.MetaTransformation;
import de.bayerl.statistics.transformer.NameAnnotation;
import de.bayerl.statistics.transformer.Transformation;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import org.reflections.Reflections;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


/**
 *  Class that creates the user interface and redirects the user's actions to the Mainapp
 */
public class MainViewController {

    @FXML
    private ComboBox transformationChoice;
    @FXML
    private HBox task;
    @FXML
    private ListView<TransformationModel> transformationListing;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private WebView webView;
    @FXML
    private ComboBox version;
    @FXML
    private Button exportButton;
    @FXML
    private Hyperlink originalTable;
    @FXML
    private Button transformButton;
    private MainApp mainApp;
    private List<Control> parameters;
    private Reflections reflections;
    private static final String SPLITTER = "  ";
    private int dragStart;
    private int lastEditId = -1;
    private int editId = -1;

    @FXML
    /**
     * Initializes the mainview
     */
    private void initialize() {
        this.reflections = new Reflections("de.bayerl.statistics.transformer");
        transformationListing.setCellFactory(list -> new MovableCell());
        redirectOutput();
        initializeTransformationChoice();
        initializeVersion();
    }

    /**
     * Fills the transformation- combobox with values and adds a listener to it
     */
    @SuppressWarnings("unchecked")
    private void initializeTransformationChoice() {
        List<String> trans = getTransformationNames();
        Collections.sort(trans);
        transformationChoice.getItems().addAll(trans);
        transformationChoice.valueProperty().addListener((observable,oldValue, newValue) -> setUpParameterFields((String) newValue));
        transformationChoice.setOnMouseClicked(event -> {
            editId = -1;
            lastEditId = -1;
        });
    }

    /**
     * Shows all controls needed for the current transformation
     *
     * @param transformationName Name of the current transformation
     */
    private void setUpParameterFields(String transformationName) {

        // create Controls-array
        parameters = parameters(task, transformationName);
        if (parameters != null) {

            // add all controls to mainview
            for (Control c : parameters) {
                task.getChildren().add(c);
            }
        }
    }

    /**
     * Fills the version-combobox
     */
    @SuppressWarnings("unchecked")
    private void initializeVersion() {
        version.getItems().add("1.1");
        version.getItems().add("1.2");
        version.getSelectionModel().select(0);
    }

    /**
     * Redirects the output, so that it's shown in a console window in the minview
     */
    private void redirectOutput() {
        consoleOutput.setStyle("-fx-color: black; -fx-text-fill: white;");
        Console console = new Console(consoleOutput);
        PrintStream ps = new PrintStream(console, true);
        System.setOut(ps);
        System.setErr(ps);
    }

    /**
     * Updates the webview to the file defined by "fileName"
     *
     * @param fileName html-file to be shown
     */
    public void updateWebView(String fileName) {
        if(fileName.equals("table_0_original.html")) {
            showOriginal();
        } else {
            webView.getEngine().load("file:///" + mainApp.getHtmlFolder() + File.separator + fileName);
        }
    }

    /**
     * Redirects the user's export command to the mainApp
     */
    @FXML
    private void export() {
        mainApp.export(version.getValue().toString());
    }

    /**
     * Enables Controls, when they can be used
     */
    public void enableControls() {
        exportButton.setDisable(false);
        originalTable.setDisable(false);
        transformButton.setDisable(false);
    }

    /**
     * Computes the amount of parameters that are needed for the currently chosen transformation
     *
     * @return A list of the correct size, filled with temporary parameter values
     */
    private List<Object> computeAmountOfParameters() {
        List<Object> tempValues = new ArrayList<>();
        for(int i = 0; i< parameters.size(); i++) {

            if(parameters.get(i) instanceof TextField) {

                // Cell??
                if (((TextField) parameters.get(i)).getPromptText().equals("Role(String)")) {
                    tempValues.add(new String[]{((TextField) parameters.get(i)).getText(),
                            ((TextField) parameters.get(i + 1)).getText()});
                    ++i;

                // String??
                } else {
                    tempValues.add(((TextField) parameters.get(i)).getText());
                }

            // String-array??
            } else if(parameters.get(i) instanceof TextArea){
                tempValues.add(((TextArea) parameters.get(i)).getText());

            // TableSliceType??
            } else {
                tempValues.add(((ComboBox) parameters.get(i)).getValue());
            }
        }
        return tempValues;
    }

    /**
     * Converts the given String-array to a list
     *
     * @param str String-array to convert
     * @return String-list
     */
    private List<String> createStringList(String[] str) {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(str));
        return list;
    }

    /**
     * Reformats Strings of the form "a-c" to Strings of the form "a,b,c,d" where a < b < c < d
     *
     * @param temp String to reformat
     * @return reformatted list of sorted integers
     */
    private List<Integer> reformatFromTo(String[] temp) {
        List<Integer> reformattedInts = new ArrayList<>();
        int start = Integer.parseInt(temp[0]);
        int end = Integer.parseInt(temp[1]);

        // first value > second value => swap
        if(start >= end) {
            int tempValue = start;
            start = end;
            end = tempValue;
        }

        // count up from start to end
        for(int j = 0; j < end - start + 1; j++) {
            reformattedInts.add(start + j);
        }

        return reformattedInts;
    }

    /**
     * Combines sequences of identic "," or "-" signs within the users input in order to react to false input
     *
     * @param userInput The users input-string
     * @param c Char to be checked
     * @return String with combined char "c"
     */
    private String combineIdentic(String userInput, char c) {
        boolean detected = false;
        boolean firstAppearenceFound = false;
        StringBuilder combined = new StringBuilder();
        for(int i = 0; i < userInput.length(); i++) {

            if(!firstAppearenceFound && userInput.charAt(i) == c) {
                firstAppearenceFound = true;
                detected = true;
                combined.append(userInput.charAt(i));
            } else if(!firstAppearenceFound && userInput.charAt(i) != c) {
                combined.append(userInput.charAt(i));
            }

            if(firstAppearenceFound) {

                // last char != c, this char == c
                if (!detected && userInput.charAt(i) == c) {
                    detected = true;
                    combined.append(userInput.charAt(i));

                    // last char == c, this char != c
                } else if (detected && userInput.charAt(i) != c) {
                    combined.append(userInput.charAt(i));
                    detected = false;

                    // last char != c, this char != c
                } else if (!detected && userInput.charAt(i) != c) {
                    combined.append(userInput.charAt(i));

                    // last char == c, this char == c
                } else if (detected && userInput.charAt(i) == c) {
                    detected = true;
                }
            }
        }
        return  combined.toString();
    }

    /**
     * Converts the given String-array to an int-list
     *
     * @param str String-array to convert
     * @return int-list
     */
    private List<Integer> createIntegerList(String[] str) {
        List<Integer> ints = new ArrayList<>();
        for(String s : str) {

            // not in format "a-c"
            if(!s.contains("-")) {
                if(s.length() != 0) {
                    ints.add(Integer.parseInt(s));
                }

            // in format "a-c"
            } else {
                String[] temp = s.split("-");
                ints.addAll(reformatFromTo(temp));
            }
        }
        Collections.sort(ints);
        Set<Integer> set = new LinkedHashSet<>(ints);
        ints = new ArrayList<>(set);
        return ints;
    }

    /**
     * Takes a list of temporary parameter-objects and converts it to the list needed by the constructor of the chosen
     * Transformation
     *
     * @param tempValues temporary parameters
     * @param c class to derive correct parameter type from
     * @return
     */
    private List<Parameter> createParameterList(List<Object> tempValues, Class c) {
        List<Parameter> parameterValues = new ArrayList<>();
        for(int i = 0; i < tempValues.size(); i++){

            // constructor needs string-array at position i
            if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("String[]")) {
                String[] str = ((String) tempValues.get(i)).split("\\n");
                parameterValues.add(new Parameter(createStringList(str)));

            // constructor needs int-array at position i
            } else if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("int[]")) {
                String s = combineIdentic(((String) tempValues.get(i)), '-');
                s = combineIdentic(s, ',');
                s = s.replaceAll(" ","");
                String[] str = s.split(",");
                parameterValues.add(new Parameter(createIntegerList(str)));

            // constructor needs Cell at position i
            } else if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("Cell")) {
                parameterValues.add(new Parameter(createStringList((String[]) tempValues.get(i))));

            // constructor String, int or TableSliceType at position i
            } else {
                parameterValues.add(new Parameter((String) tempValues.get(i)));
            }
        }

        return parameterValues;
    }

    /**
     * Check if the given string only consists of digits, "," and "-"
     *
     * @param s String to be checked
     * @return true, if string meets the requirements, false otherwise
     */
    private boolean checkIntList(String s) {
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) != '-' && s.charAt(i) != ',' && !Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if input meets the requirements
     *
     * @param tempValues list of user inputs
     * @param c class to derive requirements from
     * @return special errormsg, or "noErrors" if everything is correct
     */
    private String checkEntries(List<Object> tempValues, Class c) {
        String errorMsg = "noErrors";
        for(int i = 0; i < tempValues.size(); i++){

            // int[] required
            if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("int[]")) {
                String s = combineIdentic(((String) tempValues.get(i)), '-');
                s = combineIdentic(s, ',');
                s = s.replaceAll(" ", "");
                if(!checkIntList(s)) {
                    errorMsg = "Please only enter numbers in formats: x or x-y separated by ',' in fields described with (int[])";
                }

            // int required
            } else if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("int")) {
                String s = (String) tempValues.get(i);
                for(int j = 0 ; j < s.length(); j++) {
                    if(!Character.isDigit(s.charAt(j))) {
                        errorMsg = "Please only enter numbers in fields described with (int)";
                    }
                }
            }
        }
        return errorMsg;
    }

    /**
     * Checks user input and adds a transformation if input is correct
     */
    @FXML
    @SuppressWarnings("unchecked")
    private void handleOk() {
        List<Object> tempValues = computeAmountOfParameters();

        // get class of selected transformation
        String name = transformationChoice.getValue().toString();
        String path = "de.bayerl.statistics.transformer." + name;
        Class c = null;
        try {
            c = Class.forName(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(checkEntries(tempValues, c).equals("noErrors")) {
            List<Parameter> parameterValues = createParameterList(tempValues, c);

            // add new transformation if none is selected
            if(editId == -1) {
                mainApp.getTransformations().add(new TransformationModel(name, parameterValues));
                mainApp.getCorrespondingFileNames().add(null);
                System.out.println("Transformation" + mainApp.getTransformations().get(mainApp.getTransformations().size() - 1).getName()
                        + " added successfully");

            // edit selected transformation otherwise
            } else {
                mainApp.getTransformations().remove(editId);
                mainApp.getCorrespondingFileNames().remove(editId);
                mainApp.getTransformations().add(editId, new TransformationModel(name, parameterValues));
                mainApp.getCorrespondingFileNames().add(editId, null);
                transformationChoice.getSelectionModel().select(null);
                System.out.println("Transformation" + mainApp.getTransformations().get(editId).getName() + " edited successfully");
            }

        // show error dialog
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not add Transformation");
            alert.setContentText(checkEntries(tempValues, c));
            alert.showAndWait();
        }
    }

    /**
     * Redirect transform-command to mainApp and clear selections on transformation-list
     */
    @FXML
    private void transform() {
        mainApp.transform();
        transformationListing.getSelectionModel().select(null);

    }

    /**
     * Show the original table
     */
    @FXML
    private void showOriginal() {
        webView.getEngine().load("file:///" + mainApp.getHtmlFolder() + File.separator + "table_0_original.html");
    }

    /**
     * Creates Cell-textfields with predefined values or an empty one
     *
     * @param i control to edit
     * @return Cell textfields
     */
    private List<TextField> createCellFields(int i) {
        List<TextField> cellAttributes = new ArrayList<>();
        TextField param = new TextField();
        TextField param2 = new TextField();
        param.setPromptText("Role(String)");
        param2.setPromptText("Value(String)");

        // edit?? => set text
        if(editId != -1) {
            param.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getStringList().get(0));
            param2.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getStringList().get(1));
        }
        cellAttributes.add(param);
        cellAttributes.add(param2);
        return cellAttributes;
    }

    /**
     * Creates Combobox with predefined values or an empty one
     *
     * @param i control to edit
     * @return Cell textfields
     */
    private ComboBox<String> createTableSliceTypeBox(int i) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Row","Column");

        // edit?? => preselect correct value
        if(editId != -1) {
            if(mainApp.getTransformations().get(editId).getAttributes().get(i).getValue().equals("Column")) {
                combo.getSelectionModel().select(1);
            } else {
                combo.getSelectionModel().select(0);
            }
        } else {
            combo.getSelectionModel().select(0);
        }
        return combo;
    }

    /**
     * Creates a textarea with predefined values or an empty one
     *
     * @param i control to edit
     * @return Cell textfields
     */
    private TextArea createTextArea(Class cl, int i) {
        TextArea param = new TextArea();
        //get fieldname from transformationclass
        Annotation[] ann = cl.getConstructors()[0].getParameterAnnotations()[i];
        param.setPromptText(((NameAnnotation) ann[0]).name());

        // String[] as input required??
        if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("[]")) {
            param.setPromptText(param.getPromptText() + "[]");
            if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("String[]")) {
                param.setPromptText(param.getPromptText() + "  (Split by linebreak)");

            }
        }

        // edit?? => set text
        if(editId != -1) {
            StringBuilder builder = new StringBuilder();
            int j = 0;
            for(String str: mainApp.getTransformations().get(editId).getAttributes().get(i).getStringList()) {
                builder.append(str);

                // add every value in a new line
                if(j != mainApp.getTransformations().get(editId).getAttributes().get(i).getStringList().size() -1) {
                    builder.append("\n");
                }
                j++;
            }
            param.setText(builder.toString());
        }
        param.setMaxWidth(200);
        return param;
    }

    /**
     * Creates a textfield with predefined values or an empty one
     *
     * @param i control to edit
     * @return Cell textfields
     */
    private TextField createTextField(Class cl, int i) {
        TextField param = new TextField();

        //get fieldname from transformationclass
        Annotation[] ann = cl.getConstructors()[0].getParameterAnnotations()[i];
        param.setPromptText(((NameAnnotation) ann[0]).name());

        // int[] required
        if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("[]")) {
            param.setPromptText(param.getPromptText() + "(int[])");

            // edit?? => set text
            if(editId != -1) {
                StringBuilder builder = new StringBuilder();
                int j = 0;
                for(int h : mainApp.getTransformations().get(editId).getAttributes().get(i).getIntList()) {
                    builder.append(h);

                    // add ints separeted by ","
                    if(j != mainApp.getTransformations().get(editId).getAttributes().get(i).getIntList().size() -1) {
                        builder.append(",");
                    }
                    j++;
                }
                param.setText(builder.toString());
            }

        // int required
        } else if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("int")) {
            param.setPromptText(param.getPromptText() + "(int)");

            // edit?? => set text
            if(editId != -1) {
                param.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getValue());
            }

        // String required
        } else if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("String")) {
            param.setPromptText(param.getPromptText() + "(Str)");

            // edit?? => set text
            if(editId != -1) {
                param.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getValue());
            }
        }
        return param;
    }

    /**
     * Creates a list of Controls which are needed by the given class's constructor
     *
     * @param it Iterator that iterates through all classes that extend Transformation
     * @param newValue currently chosen transformation
     * @return List of Controls which are needed by the given class's constructor
     */
    private List<Control> createParameterFields(Iterator<Class<? extends Transformation>> it, Object newValue) {
        List<Control> parameters = new ArrayList<>();
        while (it.hasNext()) {
            Class<? extends Transformation> cl = it.next();

            //compare classname with chosen transformation-name
            if (cl.getSimpleName().equals(newValue)) {
                int params = cl.getConstructors()[0].getParameterCount();
                for (int i = 0; i < params; i++) {
                    if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("Cell")) {
                        parameters.addAll(createCellFields(i));
                    } else if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("TableSliceType")) {
                        parameters.add(createTableSliceTypeBox(i));
                    } else if(cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("String[]")){
                        parameters.add(createTextArea(cl, i));
                    } else {
                        parameters.add(createTextField(cl, i));
                    }
                }
            }
        }
        return parameters;
    }

    /**
     * Shows all parameter-fields or controls needed by the current transformation
     *
     * @param task Container of the parameter-fields
     * @param newValue transformationName
     * @return List of parameter fields needed for the transformation
     */
    private List<Control> parameters(HBox task, Object newValue) {
        //remove old parameter fields
        task.getChildren().remove(0, task.getChildren().size());
        //create class-iterator
        Iterator<Class<? extends Transformation>> it = reflections.getSubTypesOf(Transformation.class).iterator();
        return createParameterFields(it, newValue);
    }

    /**
     * Sets the mainapp for this controller
     *
     * @param mainApp desired mainapp
     */
    public void setMainApp(MainApp mainApp) {

        this.mainApp = mainApp;
        transformationListing.setItems(mainApp.getTransformations());
    }

    /**
     * Finds all currently available transformations
     *
     * @return list of available transformations
     */
    private List<String> getTransformationNames (){
        List<String> nameList = new ArrayList<>();

        // find transformations and metatransformations
        Set<Class<? extends Transformation>> classes = reflections.getSubTypesOf(Transformation.class);
        Set<Class<? extends MetaTransformation>> classes2 = reflections.getSubTypesOf(MetaTransformation.class);
        nameList.addAll(classes.stream().map(Class::getSimpleName).collect(Collectors.toList()));
        nameList.addAll(classes2.stream().map(Class::getSimpleName).collect(Collectors.toList()));
        return nameList;
    }

    /**
     * Class that defines a draggable listcell with a delete-button
     */
    private class MovableCell extends ListCell<TransformationModel> {

        private Label label;
        private Button delete = new Button(" - ");
        private Pane pane ;
        private HBox hbox;

        /**
         * Constructor for the custom ListCell
         */
        @SuppressWarnings("unchecked")
        public MovableCell() {
            ListCell thisCell = this;
            hbox = new HBox();
            label = new Label("(empty)");
            pane = new Pane();
            hbox.getChildren().addAll(label, pane, delete);
            HBox.setHgrow(pane, Priority.ALWAYS);

            setOnDragDetected(event -> {
                if (getItem() == null) {
                    return;
                }

                dragStart = transformationListing.getSelectionModel().getSelectedIndex();

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem().getName());
                dragboard.setContent(content);

                event.consume();
            });

            setOnMouseClicked(event -> {

                // Listitem clicked and html available
                if(transformationListing.getSelectionModel().getSelectedIndex() != -1 &&
                        mainApp.getCorrespondingFileNames().get(transformationListing.getSelectionModel().getSelectedIndex()) != null) {
                    File[] dir = (new File(mainApp.getHtmlFolder())).listFiles();
                    if(dir != null) {
                        for (File file : dir) {

                            // show file of given name
                            if (file.getName().contains(mainApp.getCorrespondingFileNames()
                                    .get(transformationListing.getSelectionModel().getSelectedIndex()))) {
                                updateWebView(file.getName());
                            }
                        }
                    }
                }
                lastEditId = editId;
                editId = transformationListing.getSelectionModel().getSelectedIndex();

                // edit?? => load transformation to edit
                if(editId != -1) {
                    transformationChoice.getSelectionModel().select(mainApp.getTransformations().get(editId).getName());
                }

                //last transformation chosen == transformtation to edit => reload parameterfields
                if(lastEditId != -1 && mainApp.getTransformations().get(lastEditId).getName().equals(mainApp.getTransformations().get(editId).getName())) {
                    setUpParameterFields((String) transformationChoice.getValue());
                }
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    setOpacity(1);
                }
            });

            setOnDragDropped(event -> {
                if (getItem() == null) {
                    return;
                }

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    ObservableList<TransformationModel> items = getListView().getItems();
                    int draggedIdx = dragStart;
                    int thisIdx = items.indexOf(getItem());
                    reorganizeLists(thisIdx, draggedIdx);
                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        /**
         * Reorders lists by swapping their elements depending on the users dragging
         *
         * @param thisIdx position where drag is released
         * @param draggedIdx position where drag started
         */
        protected void reorganizeLists(int thisIdx, int draggedIdx) {
            int realThisIdx = thisIdx;
            if(thisIdx > draggedIdx) {
                realThisIdx --;
            }
            mainApp.getTransformations().add(realThisIdx, mainApp.getTransformations().remove(draggedIdx));
            mainApp.getCorrespondingFileNames().add(realThisIdx, mainApp.getCorrespondingFileNames().remove(draggedIdx));
        }

        /**
         * Creates the string-representation for the given Intlist to show up in the listview
         *
         * @param intList intList to convert to string
         * @return string representation of given list
         */
        protected String createIntListName(List<Integer> intList) {
            StringBuilder intListBuilder = new StringBuilder();
            intListBuilder.append(SPLITTER + "{");
            intListBuilder.append(intList.get(0));
            if(intList.size() > 1) {
                intListBuilder.append(",");
            }
            for(int i = 1; i < intList.size() - 1; i++) {
                intListBuilder.append(intList.get(i));
                intListBuilder.append(",");
            }
            if(intList.size() > 1) {
                intListBuilder.append(intList.get(intList.size() - 1));
            }
            intListBuilder.append("}");
            return intListBuilder.toString();
        }

        /**
         * Creates the string-representation for the given stringlist to show up in the listview
         *
         * @param stringList list to convert to string
         * @return string representation of given list
         */
        protected String createStringListName(List<String> stringList) {
            StringBuilder stringListBuilder = new StringBuilder();
            stringListBuilder.append(SPLITTER);
            stringListBuilder.append("{");
            stringListBuilder.append(stringList.get(0));
            if(stringList.size() > 1) {
                stringListBuilder.append(",");
            }
            for(int i = 1; i < stringList.size() - 1; i++) {
                stringListBuilder.append(stringList.get(i));
                stringListBuilder.append(",");
            }
            if(stringList.size() > 1) {
                stringListBuilder.append(stringList.get(stringList.size() - 1));
            }
            stringListBuilder.append("}");
            return stringListBuilder.toString();
        }

        /**
         * Creates the string-representation for the given transformation
         *
         * @param item transformation to convert to string
         * @return string representation of given transformation
         */
        protected String createNameString(TransformationModel item) {
            StringBuilder transformation = new StringBuilder(item.getName());
            List<Parameter> attributes = item.getAttributes();

            // list all attribute values splitted by "  "
            for(Parameter att : attributes) {
                if(att.hasString()) {
                    transformation.append(SPLITTER);
                    transformation.append(att.getValue());
                } else if(att.hasIntList() && att.getIntList().size() > 0){
                    transformation.append(createIntListName(att.getIntList()));
                } else if(att.hasStringList() && att.getStringList().size() > 0){
                    transformation.append(createStringListName(att.getStringList()));
                }
            }
            return transformation.toString();
        }

        /**
         * Deletes the chosen item from the list of transformtions and corresponding names
         *
         * @param item item to delete
         */
        protected void delete(TransformationModel item) {
            String deleteMsg = label.getText() + " removed";
            int index = 0;

            // find index of item
            for(int i = 0; i < mainApp.getTransformations().size(); i++) {
                if(mainApp.getTransformations().get(i) == item) {
                    index = i;
                }
            }
            mainApp.getTransformations().remove(item);

            // delete corresponding html
            File[] dir = (new File(mainApp.getHtmlFolder())).listFiles();
            if(dir != null) {
                for (File file : dir) {
                    if(file.getName().contains(mainApp.getCorrespondingFileNames().get(index))) {
                        file.delete();
                    }
                }
            }
            mainApp.getCorrespondingFileNames().remove(index);
            System.out.println(deleteMsg);
        }

        /**
         * Update the ListCell on change
         *
         * @param item item of the cell
         * @param empty cell empty??
         */
        @Override
        protected void updateItem(TransformationModel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                delete.setOnAction(event -> delete(item));
                label.setText(createNameString(item));
                setGraphic(hbox);
            }
        }
    }

    /**
     * Class that represents a console and redirects System.out to the given TextArea
     */
    private static class Console extends OutputStream {

        private TextArea output;

        /**
         * Console constructor
         *
         * @param ta text-area to redirect output to
         */
        public Console(TextArea ta) {
            this.output = ta;
        }

        /**
         *  writes text to the console
         *
         * @param i content
         * @throws IOException
         */
        @Override
        public void write(int i) throws IOException {
            output.appendText(String.valueOf((char) i));
        }
    }

}
