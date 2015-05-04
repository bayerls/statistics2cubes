package de.bayerl.statistics.gui.controller;
import de.bayerl.statistics.TeiHandler;
import de.bayerl.statistics.gui.model.TransformationModel;
import de.bayerl.statistics.transformer.MetaTransformation;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.reflections.Reflections;
import java.io.*;
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
    private TeiHandler handler;
    private TextField[] parameters;
    private Reflections reflections = new Reflections("de.bayerl.statistics.transformer");
    @FXML
    private ListView<TransformationModel> transformationListing;
    @FXML
    private TextArea consoleOutput;

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
        for(String tr : trans) {
            transformationChoice.getItems().add(tr);
        }
        transformationChoice.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                parameters = parameters(task, newValue);
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        task.getChildren().add(parameters[i]);
                    }
                }
            }
        });
    }

    @FXML
    private void handleOk() {
        boolean ok = true;
        List<String> parameterValues = new ArrayList<String>();
        String name = transformationChoice.getValue().toString();
        for(TextField textField : parameters) {
            if(!textField.getText().equals("")) {
                parameterValues.add(textField.getText());
            } else {
                ok = false;
            }
        }

        if(ok) {
            mainApp.getTransformations().add(new TransformationModel(name, parameterValues));
        } else {
            System.out.println("Please fill all parameters");
        }
    }

    private TextField[] parameters(HBox task, Object newValue) {
        task.getChildren().remove(0, task.getChildren().size());
        Set<Class<? extends Transformation>> classes = reflections.getSubTypesOf(Transformation.class);
        Iterator<Class<? extends Transformation>> it = classes.iterator();
        Class<? extends Transformation> cl;
        TextField[] parameters = null;
        while (it.hasNext()) {
            cl = it.next();
            if (cl.getSimpleName().equals(newValue)) {
                int params = cl.getConstructors()[0].getParameterCount();
                parameters = new TextField[params];
                for (int i = 0; i < params; i++) {
                    parameters[i] = new TextField();
                    if(cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("int")
                            || cl.getConstructors()[0].getParameterTypes()[i].getSimpleName().equals("Integer")) {
                        parameters[i].setMaxWidth(30);
                    } else {
                        parameters[i].setPromptText("param " + i);
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

                ObservableList<TransformationModel> items = getListView().getItems();

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem().getName());
                dragboard.setContent(content);

                event.consume();
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
                    System.out.println(db.getString());
                    int draggedIdx = 0;
                    for(int i = 0; i < items.size(); i++) {
                        if(items.get(i).getName().equals(db.getString())) {
                            draggedIdx = i;
                            break;
                        }
                    }
                    int thisIdx = items.indexOf(getItem());

                    System.out.println(draggedIdx);
                    System.out.println(thisIdx);

                    TransformationModel temp = mainApp.getTransformations().get(draggedIdx);
                    mainApp.getTransformations().set(draggedIdx, mainApp.getTransformations().get(thisIdx));
                    mainApp.getTransformations().set(thisIdx, temp);

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
                        mainApp.getTransformations().remove(item);
                    }
                });
                StringBuilder transformation = new StringBuilder(item.getName() + "   ");
                for(String s : item.getAttributes()) {
                    transformation.append(" " + s);
                }
                label.setText(item != null ? transformation.toString() : "<null>");
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
