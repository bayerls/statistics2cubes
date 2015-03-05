package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

public class DeleteRowColNumbers implements Transformer {

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
