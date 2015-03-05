package de.bayerl.statistics.model;


import java.util.ArrayList;
import java.util.List;

public class Table {

    private boolean isNumbered;
    private List<Row> rows = new ArrayList<>();


    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public boolean isNumbered() {
        return isNumbered;
    }

    public void setNumbered(boolean isNumbered) {
        this.isNumbered = isNumbered;
    }
}
