package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Delete a row or column.
 */
public class DeleteRowCol extends Transformation {

    private boolean row;
    private int number;

    public DeleteRowCol(boolean row, int number) {
        this.row = row;
        this.number = number;
    }


    @Override
    public String getName() {
        return "deleteRowCol_" + row + "_" + number;
    }

    @Override
    public Table transformStep(Table table) {

        if (row) {
            table.getRows().remove(number);
        } else {
            for (Row row : table.getRows()) {
                if (row.getCells().size() > number) {
                    row.getCells().remove(number);
                }
            }
        }

        return table;
    }


}
