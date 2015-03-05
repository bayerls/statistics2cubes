package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Table;

/**
 * Sets a new type (role) for a cell.
 */
public class SetType extends Transformation {

    private String type;
    private int row;
    private int col;

    public SetType(String type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }

    @Override
    public String getName() {
        return "setType_" + type + "_" + row + "_" + col;
    }

    @Override
    protected Table transformStep(Table table) {
        table.getRows().get(row).getCells().get(col).setRole(type);

        return table;
    }
}
