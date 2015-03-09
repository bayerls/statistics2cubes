package de.bayerl.statistics.model;

import de.bayerl.statistics.converter.vocabulary.VA;

public class Header {

    private String label = "";
    private String url;
    // TODO do this right
    private String range = VA.getURI() + "cubeDimensionNominal";

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
