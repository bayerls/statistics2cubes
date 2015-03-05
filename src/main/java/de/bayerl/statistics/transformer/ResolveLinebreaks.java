package de.bayerl.statistics.transformer;

import de.bayerl.statistics.TeiLoader;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Replaces line break placeholders.
 */
public class ResolveLinebreaks extends Transformation {

    @Override
    public String getName() {
        return "resolveLinebreaks";
    }

    @Override
    public Table transformStep(Table table) {
        for (Row row : table.getRows()) {
            for (Cell cell : row.getCells()) {
                String value = cell.getValue().getValue();
                value = value.replace("-" + TeiLoader.PLACEHOLDER_LB, "");
                value = value.replace(TeiLoader.PLACEHOLDER_LB, " ");
                cell.getValue().setValue(value);
            }
        }

        return table;
    }

}
