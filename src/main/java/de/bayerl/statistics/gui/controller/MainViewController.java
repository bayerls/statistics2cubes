package de.bayerl.statistics.gui.controller;
import de.bayerl.statistics.gui.model.Parameter;
import de.bayerl.statistics.gui.model.TransformationModel;
import de.bayerl.statistics.transformer.MetaTransformation;
import de.bayerl.statistics.transformer.NameAnnotation;
import de.bayerl.statistics.transformer.Transformation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.reflections.Reflections;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.List;

/**
 * Created by Alexander on 28.04.2015.
 */
public class MainViewController {

    @FXML
    private ComboBox transformationChoice;
    @FXML
    private HBox task;
    private MainApp mainApp;
    private List<Control> parameters;
    private Reflections reflections = new Reflections("de.bayerl.statistics.transformer");
    @FXML
    private ListView<TransformationModel> transformationListing;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private WebView webView;
    private static final String SPLITTER = "~#~LB~#~";
    private int dragStart;
    @FXML
    private ComboBox version;
    @FXML
    private Button exportButton;
    @FXML
    private Hyperlink originalTable;
    @FXML
    private Button transformButton;

    @FXML
    private void initialize() {
        transformationListing.setCellFactory(new Callback<ListView<TransformationModel>, ListCell<TransformationModel>>() {
            @Override
            public ListCell<TransformationModel> call(ListView<TransformationModel> param) {
                return new MovableCell();
            }
        });
        consoleOutput.setStyle("-fx-color: black; -fx-text-fill: white;");
        Console console = new Console(consoleOutput);
        PrintStream ps = new PrintStream(console, true);
        System.setOut(ps);
        System.setErr(ps);
        List<String> trans = getTransformationNames();
        version.getItems().add("1.1");
        version.getItems().add("1.2");
        version.getSelectionModel().select(0);
        Collections.sort(trans);
        for(String tr : trans) {
            transformationChoice.getItems().add(tr);
        }
        transformationChoice.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                parameters = parameters(task, newValue);
                if (parameters != null) {
                    for (int i = 0; i < parameters.size(); i++) {
                        task.getChildren().add(parameters.get(i));
                    }
                }
            }
        });
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

    @FXML
    private void handleOk() {
        boolean ok = true;
        List<Object> tempValues = new ArrayList<>();
        String name = transformationChoice.getValue().toString();
        for(int i = 0; i< parameters.size(); i++) {
            if(parameters.get(i) instanceof TextField) {
                if (((TextField) parameters.get(i)).getPromptText().equals("Role")) {
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

        String path = "de.bayerl.statistics.transformer." + name;
        Class c = null;
        try {
            c = Class.forName(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Parameter> parameterValues = new ArrayList<>();
        for(int i = 0; i < tempValues.size(); i++){
            if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("String[]")) {
                String[] str = ((String) tempValues.get(i)).split("\\n");
                List<String> list = new ArrayList<>();
                for(String s : str) {
                    list.add(s);
                }
                Set<String> set = new LinkedHashSet<>(list);
                list = new ArrayList<>(set);
                parameterValues.add(new Parameter(list));
            } else if(c.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("int[]")) {
                String[] str = ((String) tempValues.get(i)).replaceAll(" ", "").split(",");
                List<Integer> ints = new ArrayList<>();
                for(String s : str) {
                    if(!s.contains("-")) {
                        ints.add(Integer.parseInt(s));
                    } else {
                        String[] temp = s.split("-");
                        int start = Integer.parseInt(temp[0]);
                        int end = Integer.parseInt(temp[1]);
                        if(start >= end) {
                            int tempValue = start;
                            start = end;
                            end = tempValue;
                        }
                        for(int j = 0; j < end - start + 1; j++) {
                            ints.add(start + j);
                        }
                    }
                }
                Collections.sort(ints);
                parameterValues.add(new Parameter(ints));
            } else {
                parameterValues.add(new Parameter((String) tempValues.get(i)));
            }
        }

        if(ok) {
            mainApp.getTransformations().add(new TransformationModel(name, parameterValues));
            mainApp.getCorrespondingFileNames().add(null);
        } else {
            System.out.println("Please fill all parameters");
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

    private List<Control> parameters(HBox task, Object newValue) {

        task.getChildren().remove(0, task.getChildren().size());
        Set<Class<? extends Transformation>> classes = reflections.getSubTypesOf(Transformation.class);
        Iterator<Class<? extends Transformation>> it = classes.iterator();
        Class<? extends Transformation> cl;
        List<Control> parameters = new ArrayList<>();
        while (it.hasNext()) {
            cl = it.next();
            if (cl.getSimpleName().equals(newValue)) {
                int params = cl.getConstructors()[0].getParameterCount();
                for (int i = 0; i < params; i++) {
                    if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("Cell")) {
                        TextField param = new TextField();
                        TextField param2 = new TextField();
                        param.setPromptText("Role");
                        param2.setPromptText("Value");
                        parameters.add(param);
                        parameters.add(param2);
                    } else if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("TableSliceType")) {
                        ComboBox<String> combo = new ComboBox<>();
                        combo.getItems().addAll("Row","Column");
                        combo.getSelectionModel().select(0);
                        parameters.add(combo);
                    } else {
                        if(cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("String[]")){
                            TextArea param = new TextArea();
                            Annotation[] ann = cl.getConstructors()[0].getParameterAnnotations()[i];
                            param.setPromptText(((NameAnnotation) ann[0]).name());
                            if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("[]")) {
                                param.setPromptText(param.getPromptText() + "[]");
                            }
                            param.setMaxWidth(200);
                            parameters.add(param);
                        } else {
                            TextField param = new TextField();
                            Annotation[] ann = cl.getConstructors()[0].getParameterAnnotations()[i];
                            param.setPromptText(((NameAnnotation) ann[0]).name());
                            if (cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().contains("[]")) {
                                param.setPromptText(param.getPromptText() + "[]");
                            }
                            parameters.add(param);
                        }
                    }
                }
            }
        }
        return parameters;
    }

    public void setMainApp(MainApp mainApp) {

        this.mainApp = mainApp;
        transformationListing.setItems(mainApp.getTransformations());
    }

    private List<String> getTransformationNames (){
        List<String> nameList = new ArrayList<String>();
        Set<Class<? extends Transformation>> classes = reflections.getSubTypesOf(Transformation.class);
        Iterator<Class<? extends Transformation>> it = classes.iterator();

        while(it.hasNext()) {
            Class<? extends  Transformation> cl = it.next();
            nameList.add(cl.getSimpleName());
        }

        Set<Class<? extends MetaTransformation>> classes2 = reflections.getSubTypesOf(MetaTransformation.class);
        Iterator<Class<? extends MetaTransformation>> it2 = classes2.iterator();

        while(it2.hasNext()) {

            Class<? extends MetaTransformation> cl = it2.next();
            nameList.add(cl.getSimpleName());

        }
        return nameList;
    }

    private class MovableCell extends ListCell<TransformationModel> {

        private Label label;
        private Button delete = new Button(" - ");
        private Pane pane ;
        private HBox hbox;
        private String secretText;

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
                    for (File file : dir) {
                        if (file.getName().contains(mainApp.getCorrespondingFileNames()
                                .get(transformationListing.getSelectionModel().getSelectedIndex()))) {
                            updateWebView(file.getName());
                        }
                    }
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
                    TransformationModel temp = mainApp.getTransformations().get(draggedIdx);
                    String tempString = mainApp.getCorrespondingFileNames().get(draggedIdx);
                    mainApp.getTransformations().set(draggedIdx, mainApp.getTransformations().get(thisIdx));
                    mainApp.getCorrespondingFileNames().set(draggedIdx, mainApp.getCorrespondingFileNames().get(thisIdx));
                    mainApp.getTransformations().set(thisIdx, temp);
                    mainApp.getCorrespondingFileNames().set(thisIdx, tempString);

                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(TransformationModel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                delete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        System.out.println(label.getText() + " removed");
                        int index = 0;
                        for(int i = 0; i < mainApp.getTransformations().size(); i++) {
                            if(mainApp.getTransformations().get(i) == item) {
                                index = i;
                            }
                        }
                        mainApp.getCorrespondingFileNames().remove(index);
                        mainApp.getTransformations().remove(item);
                    }
                });
                StringBuilder transformation = new StringBuilder(item.getName());
                List<Parameter> attributes = item.getAttributes();
                for(Parameter att : attributes) {
                    if(att.hasString()) {
                        transformation.append(SPLITTER + att.getValue());
                    } else if(att.hasIntList() && att.getIntList().size() > 0){
                        transformation.append(SPLITTER + "{");
                        transformation.append(att.getIntList().get(0));
                        if(att.getIntList().size() > 1) {
                            transformation.append(",");
                        }
                        for(int i = 1; i < att.getIntList().size() - 1; i++) {
                            transformation.append(att.getIntList().get(i) + ",");
                        }
                        if(att.getIntList().size() > 1) {
                            transformation.append(att.getIntList().get(att.getIntList().size() - 1));
                        }
                        transformation.append("}");
                    } else if(att.hasStringList() && att.getStringList().size() > 0){
                        transformation.append(SPLITTER + "{");
                        transformation.append(att.getStringList().get(0));
                        if(att.getStringList().size() > 1) {
                            transformation.append(",");
                        }
                        for(int i = 1; i < att.getStringList().size() - 1; i++) {
                            transformation.append(att.getStringList().get(i) + ",");
                        }
                        if(att.getStringList().size() > 1) {
                            transformation.append(att.getStringList().get(att.getStringList().size() - 1));
                        }
                        transformation.append("}");
                    }
                }
                secretText = transformation.toString();
                label.setText(item != null ? secretText.replaceAll(SPLITTER, "  ") : "<null>");
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
