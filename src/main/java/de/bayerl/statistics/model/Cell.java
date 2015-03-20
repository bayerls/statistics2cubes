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

}
