package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Table;

/**
 * Sets a new value in a cell.
 */
public class SetValue extends Transformation {

    private String value;
    private int row;
    private int col;

    public SetValue(String value, int row, int col) {
        this.value = value;
        this.row = row;
        this.col = col;
    }

    @Override
    public String getName() {
        return "setValue_" + value + "_" + row + "_" + col;
    }

    @Override
    protected Table transformStep(Table table) {
        table.getRows().get(row).getCells().get(col).getValue().setValue(value);

        return table;
    }
}
