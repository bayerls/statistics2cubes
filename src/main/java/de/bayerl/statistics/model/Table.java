package de.bayerl.statistics.model;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Table {

    private boolean isNumbered;
    private Metadata metadata = new Metadata();
    private List<Header> headers = new ArrayList<>();
    private List<Row> rows = new ArrayList<>();

}
