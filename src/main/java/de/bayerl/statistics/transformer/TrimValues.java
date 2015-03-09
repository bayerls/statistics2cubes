package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Trims the value of every cell.
 */
public class TrimValues extends Transformation {

    @Override
    public String getName() {
        return "trimValues";
    }

    @Override
    protected Table transformStep(Table table) {
        for (Row row : table.getRows()) {
            for (Cell cell : row.getCells()) {
                cell.getValue().setValue(cell.getValue().getValue().trim());
            }
        }

        return table;
    }
}
