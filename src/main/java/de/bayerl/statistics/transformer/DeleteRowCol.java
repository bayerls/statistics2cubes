package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.TableSliceType;

/**
 * Delete a row or column.
 */
public class DeleteRowCol extends Transformation {

    private TableSliceType tableSliceType;
    private int number;

    public DeleteRowCol(TableSliceType tableSliceType, @NameAnnotation(name = "number") int number) {
        this.tableSliceType = tableSliceType;
        this.number = number;
    }


    @Override
    public String getName() {
        return "deleteRowCol_" + tableSliceType + "_" + number;
    }

    @Override
    public Table transformStep(Table table) {

        if (tableSliceType == TableSliceType.ROW) {
            table.getRows().remove(number);
        } else {
            table.getRows().stream().filter(row -> row.getCells().size() > number).forEach(row -> row.getCells().remove(number));
        }

        return table;
    }


}
