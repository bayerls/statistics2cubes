package de.bayerl.statistics.model;


import java.util.ArrayList;
import java.util.List;

public class Table {

    private boolean isNumbered;
    private Metadata metadata = new Metadata();
    private List<Header> headers = new ArrayList<>();
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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

}
