package de.bayerl.statistics.model;


import lombok.Data;

import java.io.Serializable;

@Data
public class Cell implements Serializable {

    private Value value = new Value();
    private String role = "";
    private String rend;
    private int rows;
    private int cols;

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRend() {
        return rend;
    }

    public void setRend(String rend) {
        this.rend = rend;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}
