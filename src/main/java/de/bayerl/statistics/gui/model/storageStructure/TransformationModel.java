package de.bayerl.statistics.gui.model.storageStructure;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class TransformationModel {

    private StringProperty name;
    private List<Parameter> attributes;

    public TransformationModel() {
        this.name = new SimpleStringProperty();
        this.attributes = new ArrayList<>();
    }

    public TransformationModel(String name, List<Parameter> attributes) {
        this.name = new SimpleStringProperty(name);
        this.attributes = attributes;
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

    public List<Parameter> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Parameter> attributes) {
        this.attributes = attributes;
    }
}
