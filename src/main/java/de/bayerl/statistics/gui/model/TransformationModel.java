package de.bayerl.statistics.gui.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class TransformationModel {

    private final StringProperty name;
    private ListProperty<String> attributes;

    public TransformationModel() {
        this.name = new SimpleStringProperty();
        this.attributes = new SimpleListProperty<>();
    }

    public TransformationModel(String name, List<String> attributes) {
        this.name = new SimpleStringProperty(name);
        ObservableList<String> obs = FXCollections.observableArrayList(attributes);
        this.attributes = new SimpleListProperty<String>(obs);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ObservableList<String> getAttributes() {
        return attributes.get();
    }

    public ListProperty<String> attributesProperty() {
        return attributes;
    }

    public void setAttributes(ObservableList<String> attributes) {
        this.attributes.set(attributes);
    }
}
