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

}
