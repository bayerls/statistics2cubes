package de.bayerl.statistics.gui.model;

import java.util.List;

public class Parameter {
    private List<String> stringList;
    private List<Integer> intList;
    private String value;
    private boolean hasStringList;
    private boolean hasIntList;
    private boolean hasString;

    public Parameter() {
    }

    @SuppressWarnings("unchecked")
    public Parameter(List list) {
        if(!list.isEmpty() && list.get(0).getClass().getSimpleName().equals("String")) {
            this.stringList = list;
            hasIntList = false;
            hasString = false;
            hasStringList = true;
        } else {
            this.intList = list;
            hasIntList = true;
            hasString = false;
            hasStringList = false;
        }
    }

    public Parameter(String string) {
        this.value = string;
        hasIntList = false;
        hasString = true;
        hasStringList = false;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public List<Integer> getIntList() {
        return intList;
    }

    public void setIntList(List<Integer> intList) {
        this.intList = intList;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean hasStringList() {
        return hasStringList;
    }

    public void sethasStringList(boolean hasStringList) {
        this.hasStringList = hasStringList;
    }

    public boolean hasIntList() {
        return hasIntList;
    }

    public void setHasIntList(boolean hasIntList) {
        this.hasIntList = hasIntList;
    }

    public boolean hasString() {
        return hasString;
    }

    public void setHasString(boolean hasString) {
        this.hasString = hasString;
    }

}
