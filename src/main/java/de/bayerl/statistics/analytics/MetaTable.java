package de.bayerl.statistics.analytics;

import lombok.Data;

/**
 * Created by sebastianbayerl on 08/04/15.
 */
@Data
public class MetaTable {

    private String title;
    private String file;
    private int linkGroupSize;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLinkGroupSize() {
        return linkGroupSize;
    }

    public void setLinkGroupSize(int linkGroupSize) {
        this.linkGroupSize = linkGroupSize;
    }

}
