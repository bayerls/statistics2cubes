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
    private ListProperty<Parameter> attributes;

    public TransformationModel() {
        this.name = new SimpleStringProperty();
        this.attributes = new SimpleListProperty<>();
    }

    public TransformationModel(String name, List<Parameter> attributes) {
        this.name = new SimpleStringProperty(name);
        ObservableList<Parameter> obs = FXCollections.observableArrayList(attributes);
        this.attributes = new SimpleListProperty<>(obs);
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

    public ObservableList<Parameter> getAttributes() {
        return attributes.get();
    }

    public ListProperty<Parameter> attributesProperty() {
        return attributes;
    }

    public void setAttributes(ObservableList<Parameter> attributes) {
        this.attributes.set(attributes);
    }
}
