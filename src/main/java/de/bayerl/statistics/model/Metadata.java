package de.bayerl.statistics.model;

import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Metadata {

    private String label;
    private String description;
    private String importer;

    private Timestamp cubeCreated = new Timestamp(new Date().getTime());
    private String title;
    private String distributor;
    private String license;
    private String language;
    private List<String> sources = new ArrayList<>();

}
