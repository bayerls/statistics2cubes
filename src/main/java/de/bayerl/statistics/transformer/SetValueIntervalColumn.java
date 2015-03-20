package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Table;

/**
 * Sets new values for an interval in a column. Interval boarders are included.
 */
public class SetValueIntervalColumn extends Transformation {

    private int colNumber;
    private int from;
    private int to;
    private String value;

    public SetValueIntervalColumn(int colNumber, int from, int to, String value) {
        this.colNumber = colNumber;
        this.from = from;
        this.to = to;
        this.value = value;
    }

    @Override
    public String getName() {
        return "setValueInterval";
    }

    @Override
    protected Table transformStep(Table table) {
        for (int i = from; i <= to; i++) {
            Cell cell = table.getRows().get(i).getCells().get(colNumber);
            cell.getValue().setValue(value);
        }

        return table;
    }
}
