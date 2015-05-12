package de.bayerl.statistics.model;


import lombok.Data;

import java.io.Serializable;

@Data
public class Value implements Serializable {

    private boolean isMeasure;
    private String measureType;
    private String measureUnit;
    private String url;
    private boolean isNum;
    private String numType;
    private String value = "";

    public boolean isMeasure() {
        return isMeasure;
    }

    public void setMeasure(boolean isMeasure) {
        this.isMeasure = isMeasure;
    }

    public String getMeasureType() {
        return measureType;
    }

    public void setMeasureType(String measureType) {
        this.measureType = measureType;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isNum() {
        return isNum;
    }

    public void setNum(boolean isNum) {
        this.isNum = isNum;
    }

    public String getNumType() {
        return numType;
    }

    public void setNumType(String numType) {
        this.numType = numType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
