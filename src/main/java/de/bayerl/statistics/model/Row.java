package de.bayerl.statistics.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Row {

    private String rend;
    private List<Cell> cells = new ArrayList<>();

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
