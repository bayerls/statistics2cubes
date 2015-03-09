package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Removes the enumerations for rows and columns.
 */
public class DeleteRowColNumbers implements MetaTransformation {

    @Override
    public Table transform(Table table) {
        table.setNumbered(false);
        table.getRows().remove(0);

        for (Row row : table.getRows()) {
            row.getCells().remove(0);
        }

        return table;
    }
}
