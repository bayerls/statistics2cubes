package de.bayerl.statistics.gui.controller;

import de.bayerl.statistics.model.*;
import de.bayerl.statistics.transformer.DeepCopyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that clones a Table
 */
public class TableCloner {

    private DeepCopyUtil dcu;

    /**
     * Creates a new TableCloner
     */
    public TableCloner() {
        this.dcu = new DeepCopyUtil();
    }

    public Table clone(Table t) {
        Table cloned = new Table();

        Metadata m = cloneMetadata(t);
        cloned.setMetadata(m);

        // clone headers
        cloned.setHeaders(new ArrayList<>());
        for(Header h : t.getHeaders()) {
            cloned.getHeaders().add(cloneHeader(h));
        }

        boolean isNumbered = t.isNumbered();
        cloned.setNumbered(isNumbered);

        // clone rows
        cloned.setRows(new ArrayList<>());
        for(Row r : t.getRows()) {
            cloned.getRows().add(cloneRow(r));
        }

        return cloned;
    }

    /**
     * Clones the metadata of the given table
     *
     * @param t Table to clone
     * @return cloned metadata
     */
    private Metadata cloneMetadata(Table t) {
        Metadata m = new Metadata();
        m.setLabel(t.getMetadata().getLabel());
        m.setDescription(t.getMetadata().getDescription());
        m.setImporter(t.getMetadata().getImporter());
        m.setCubeCreated(t.getMetadata().getCubeCreated());
        m.setTitle(t.getMetadata().getTitle());
        m.setDistributor(t.getMetadata().getDistributor());
        m.setLicense(t.getMetadata().getLicense());
        m.setLanguage(t.getMetadata().getLanguage());
        m.setSources(new ArrayList<>());
        for(String str : t.getMetadata().getSources()) {
            m.getSources().add(str);
        }
        return m;
    }

    /**
     * Clones the given header
     *
     * @param h header to clone
     * @return cloned header
     */
    private Header cloneHeader(Header h) {
        Header header = new Header();
        header.setLabel(h.getLabel());
        header.setRange(h.getRange());
        header.setUrl(h.getUrl());
        return header;
    }

    /**
     * Clones the given row
     *
     * @param r row to clone
     * @return cloned row
     */
    private Row cloneRow(Row r) {
        Row row = new Row();
        row.setRend(r.getRend());
        row.setCells(new ArrayList<>());

        // clone all cells
        for(Cell c : r.getCells()) {
            row.getCells().add(dcu.deepCopy(c));
        }
        return row;
    }
}
