package de.bayerl.statistics.gui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander on 04.05.2015.
 */
public class Parameter {
    private List<String> stringList;
    private List<Integer> intList;
    private String value;
    private int intValue;
    private boolean hasStringList;
    private boolean hasIntList;
    private boolean hasString;
    private boolean hasIntValue;

    public Parameter() {

    }

    public Parameter(List list) {
        if(list.get(0).getClass().getSimpleName().equals("String")) {
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
        hasIntValue = false;
    }

    public Parameter(String string) {
        this.value = string;
        hasIntList = false;
        hasString = true;
        hasStringList = false;
        hasIntValue = false;
    }

    public Parameter(int intValue) {
        this.intValue = intValue;
        hasIntList = false;
        hasString = true;
        hasStringList = false;
        hasIntValue = true;
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

    public boolean hasIntValue() {
        return hasIntValue;
    }

    public void setHasIntValue(boolean hasIntValue) {
        this.hasIntValue = hasIntValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
}