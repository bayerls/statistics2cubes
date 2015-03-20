package de.bayerl.statistics.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Row {

    private String rend;
    private List<Cell> cells = new ArrayList<>();

}
