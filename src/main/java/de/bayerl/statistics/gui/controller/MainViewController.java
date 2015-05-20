package de.bayerl.statistics.gui.controller;
import de.bayerl.statistics.gui.model.Parameter;
import de.bayerl.statistics.gui.model.TransformationModel;
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
    private void initialize() {
        this.reflections = new Reflections("de.bayerl.statistics.transformer");
        transformationListing.setCellFactory(list -> new MovableCell());
        redirectOutput();
        initializeTransformationChoice();
        initializeVersion();
    }

    @SuppressWarnings("unchecked")
    private void initializeTransformationChoice() {
        List<String> trans = getTransformationNames();
        Collections.sort(trans);
        transformationChoice.getItems().addAll(trans);
        transformationChoice.valueProperty().addListener((observable,oldValue, newValue) -> {
            setUpParameterFields((String) newValue);
        });
        transformationChoice.setOnMouseClicked(event -> {
            editId = -1;
            lastEditId = -1;
        });
    }

    private void setUpParameterFields(String s) {
        parameters = parameters(task, s);
        if (parameters != null) {
            for (Control c : parameters) {
                task.getChildren().add(c);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeVersion() {
        version.getItems().add("1.1");
        version.getItems().add("1.2");
        version.getSelectionModel().select(0);
    }

    private void redirectOutput() {
        consoleOutput.setStyle("-fx-color: black; -fx-text-fill: white;");
        Console console = new Console(consoleOutput);
        PrintStream ps = new PrintStream(console, true);
        System.setOut(ps);
        System.setErr(ps);
    }

    public void updateWebView(String fileName) {
        if(fileName.equals("table_0_original.html")) {
            showOriginal();
        } else {
            webView.getEngine().load("file:///" + mainApp.getHtmlFolder() + File.separator + fileName);
        }
    }

    @FXML
    private void export() {
        mainApp.export(version.getValue().toString());
    }

    public void enableControls() {
        exportButton.setDisable(false);
        originalTable.setDisable(false);
        transformButton.setDisable(false);
    }

    private List<Object> computeAmountOfParameters() {
        List<Object> tempValues = new ArrayList<>();
        for(int i = 0; i< parameters.size(); i++) {
            if(parameters.get(i) instanceof TextField) {
                if (((TextField) parameters.get(i)).getPromptText().equals("Role(String)")) {
                    tempValues.add(new String[]{((TextField) parameters.get(i)).getText(),
                            ((TextField) parameters.get(i + 1)).getText()});
                    ++i;
                } else {
                    tempValues.add(((TextField) parameters.get(i)).getText());
                }
            } else if(parameters.get(i) instanceof TextArea){
                tempValues.add(((TextArea) parameters.get(i)).getText());
            } else {
                tempValues.add(((ComboBox) parameters.get(i)).getValue());
            }
        }
        return tempValues;
    }

    private List<String> createStringList(String[] str) {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(str));
        return list;
    }

    private List<Integer> reformatFromTo(String[] temp) {
        List<Integer> reformattedInts = new ArrayList<>();
        int start = Integer.parseInt(temp[0]);
        int end = Integer.parseInt(temp[1]);
        if(start >= end) {
            int tempValue = start;
            start = end;
            end = tempValue;
        }
        for(int j = 0; j < end - start + 1; j++) {
            reformattedInts.add(start + j);
        }
        return reformattedInts;
    }

    private String combineIdentic(String s, char c) {
        boolean detected = false;
        StringBuilder combined = new StringBuilder();
        for(int i = 0; i < s.length(); i++) {
            if(!detected && s.charAt(i) == c) {
                detected = true;
                combined.append(s.charAt(i));
            } else if(detected && s.charAt(i) != c) {
                combined.append(s.charAt(i));
                detected = false;
            } else if(!detected && s.charAt(i) != c) {
                combined.append(s.charAt(i));
            } else if(detected && s.charAt(i) == c) {
                detected = true;
            }
        }
        return  combined.toString();
    }

    private List<Integer> createIntegerList(String[] str) {
        List<Integer> ints = new ArrayList<>();
        for(String s : str) {
            if(!s.contains("-")) {
                if(s.length() != 0) {
                    ints.add(Integer.parseInt(s));
                }
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

    private List<Parameter> createParameterList(List<Object> tempValues, Class c) {
        List<Parameter> parameterValues = new ArrayList<>();
        for(int i = 0; i < tempValues.size(); i++){
            if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("String[]")) {
                String[] str = ((String) tempValues.get(i)).split("\\n");
                parameterValues.add(new Parameter(createStringList(str)));
            } else if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("int[]")) {
                String s = combineIdentic(((String) tempValues.get(i)), '-');
                s = combineIdentic(s, ',');
                s = s.replaceAll(" ","");
                String[] str = s.split(",");
                parameterValues.add(new Parameter(createIntegerList(str)));
            } else if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("Cell")) {
                parameterValues.add(new Parameter(createStringList((String[]) tempValues.get(i))));
            } else {
                parameterValues.add(new Parameter((String) tempValues.get(i)));
            }
        }

        return parameterValues;
    }

    private boolean checkIntList(String s) {
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) != '-' && s.charAt(i) != ',' && !Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String checkEntries(List<Object> tempValues, Class c) {
        String errorMsg = "noErrors";
        for(int i = 0; i < tempValues.size(); i++){
            if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("int[]")) {
                String s = combineIdentic(((String) tempValues.get(i)), '-');
                s = combineIdentic(s, ',');
                s = s.replaceAll(" ", "");
                if(!checkIntList(s)) {
                    errorMsg = "Please only enter numbers in formats: x or x-y separated by ',' in fields described with (int[])";
                }
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

    @FXML
    private void handleOk() {
        List<Object> tempValues = computeAmountOfParameters();

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
            if(editId == -1) {
                mainApp.getTransformations().add(new TransformationModel(name, parameterValues));
                mainApp.getCorrespondingFileNames().add(null);
                System.out.println("Transformation" + mainApp.getTransformations().get(mainApp.getTransformations().size() - 1).getName()
                        + " added successfully");
            } else {
                mainApp.getTransformations().remove(editId);
                mainApp.getCorrespondingFileNames().remove(editId);
                mainApp.getTransformations().add(editId, new TransformationModel(name, parameterValues));
                mainApp.getCorrespondingFileNames().add(editId, null);
                transformationChoice.getSelectionModel().select(null);
                System.out.println("Transformation" + mainApp.getTransformations().get(editId).getName() + " edited successfully");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not add Transformation");
            alert.setContentText(checkEntries(tempValues, c));
            alert.showAndWait();
        }
    }

    @FXML
    private void transform() {
        mainApp.transform();
        transformationListing.getSelectionModel().select(null);

    }

    @FXML
    private void showOriginal() {
        webView.getEngine().load("file:///" + mainApp.getHtmlFolder() + File.separator + "table_0_original.html");
    }

    private List<TextField> createCellFields(int i) {
        List<TextField> cellAttributes = new ArrayList<>();
        TextField param = new TextField();
        TextField param2 = new TextField();
        param.setPromptText("Role(String)");
        param2.setPromptText("Value(String)");
        if(editId != -1) {
            param.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getStringList().get(0));
            param2.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getStringList().get(1));
        }
        cellAttributes.add(param);
        cellAttributes.add(param2);
        return cellAttributes;
    }

    private ComboBox<String> createTableSliceTypeBox(int i) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Row","Column");
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

    private TextArea createTextArea(Class cl, int i) {
        TextArea param = new TextArea();
        //get fieldname from transformationclass
        Annotation[] ann = cl.getConstructors()[0].getParameterAnnotations()[i];
        param.setPromptText(((NameAnnotation) ann[0]).name());
        if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("[]")) {
            param.setPromptText(param.getPromptText() + "[]");
            if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("String[]")) {
                param.setPromptText(param.getPromptText() + "  (Split by linebreak)");

            }
        }
        if(editId != -1) {
            StringBuilder builder = new StringBuilder();
            int j = 0;
            for(String str: mainApp.getTransformations().get(editId).getAttributes().get(i).getStringList()) {
                builder.append(str);
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

    private TextField createTextField(Class cl, int i) {
        TextField param = new TextField();
        //get fieldname from transformationclass
        Annotation[] ann = cl.getConstructors()[0].getParameterAnnotations()[i];
        param.setPromptText(((NameAnnotation) ann[0]).name());
        if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("[]")) {
            param.setPromptText(param.getPromptText() + "(int[])");
            if(editId != -1) {
                StringBuilder builder = new StringBuilder();
                int j = 0;
                for(int h : mainApp.getTransformations().get(editId).getAttributes().get(i).getIntList()) {
                    builder.append(h);
                    if(j != mainApp.getTransformations().get(editId).getAttributes().get(i).getIntList().size() -1) {
                        builder.append(",");
                    }
                    j++;
                }
                param.setText(builder.toString());
            }
        } else if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("int")) {
            param.setPromptText(param.getPromptText() + "(int)");
            if(editId != -1) {
                param.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getValue());
            }
        } else if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("String")) {
            param.setPromptText(param.getPromptText() + "(Str)");
            if(editId != -1) {
                param.setText(mainApp.getTransformations().get(editId).getAttributes().get(i).getValue());
            }
        }
        return param;
    }

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

    private List<Control> parameters(HBox task, Object newValue) {
        //remove old parameter fields
        task.getChildren().remove(0, task.getChildren().size());
        //create class-iterator
        Iterator<Class<? extends Transformation>> it = reflections.getSubTypesOf(Transformation.class).iterator();
        return createParameterFields(it, newValue);
    }

    public void setMainApp(MainApp mainApp) {

        this.mainApp = mainApp;
        transformationListing.setItems(mainApp.getTransformations());
    }

    private List<String> getTransformationNames (){
        List<String> nameList = new ArrayList<>();
        Set<Class<? extends Transformation>> classes = reflections.getSubTypesOf(Transformation.class);
        Set<Class<? extends MetaTransformation>> classes2 = reflections.getSubTypesOf(MetaTransformation.class);
        nameList.addAll(classes.stream().map(Class::getSimpleName).collect(Collectors.toList()));
        nameList.addAll(classes2.stream().map(Class::getSimpleName).collect(Collectors.toList()));
        return nameList;
    }

    private class MovableCell extends ListCell<TransformationModel> {

        private Label label;
        private Button delete = new Button(" - ");
        private Pane pane ;
        private HBox hbox;

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
                if(mainApp.getCorrespondingFileNames().get(transformationListing.getSelectionModel().getSelectedIndex()) != null) {
                    File[] dir = (new File(mainApp.getHtmlFolder())).listFiles();
                    if(dir != null) {
                        for (File file : dir) {
                            if (file.getName().contains(mainApp.getCorrespondingFileNames()
                                    .get(transformationListing.getSelectionModel().getSelectedIndex()))) {
                                updateWebView(file.getName());
                            }
                        }
                    }
                }
                lastEditId = editId;
                editId = transformationListing.getSelectionModel().getSelectedIndex();
                transformationChoice.getSelectionModel().select(mainApp.getTransformations().get(editId).getName());
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

        protected void reorganizeLists(int thisIdx, int draggedIdx) {
            TransformationModel temp = mainApp.getTransformations().get(draggedIdx);
            String tempString = mainApp.getCorrespondingFileNames().get(draggedIdx);
            mainApp.getTransformations().set(draggedIdx, mainApp.getTransformations().get(thisIdx));
            mainApp.getCorrespondingFileNames().set(draggedIdx, mainApp.getCorrespondingFileNames().get(thisIdx));
            mainApp.getTransformations().set(thisIdx, temp);
            mainApp.getCorrespondingFileNames().set(thisIdx, tempString);
        }

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

        protected String createNameString(TransformationModel item) {
            StringBuilder transformation = new StringBuilder(item.getName());
            List<Parameter> attributes = item.getAttributes();
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

        protected void delete(TransformationModel item) {
            String deleteMsg = label.getText() + " removed";
            int index = 0;
            for(int i = 0; i < mainApp.getTransformations().size(); i++) {
                if(mainApp.getTransformations().get(i) == item) {
                    index = i;
                }
            }
            mainApp.getCorrespondingFileNames().remove(index);
            mainApp.getTransformations().remove(item);
            System.out.println(deleteMsg);
        }

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

    private static class Console extends OutputStream {

        private TextArea output;

        public Console(TextArea ta) {
            this.output = ta;
        }

        @Override
        public void write(int i) throws IOException {
            output.appendText(String.valueOf((char) i));
        }
    }

}
