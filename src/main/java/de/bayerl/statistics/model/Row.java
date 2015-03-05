package de.bayerl.statistics.model;

import java.util.ArrayList;
import java.util.List;

public class Row {

    private Table table;
    private String rend;

    private List<Cell> cells = new ArrayList<>();

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

    public String getRend() {
        return rend;
    }

    public void setRend(String rend) {
        this.rend = rend;
    }
}
