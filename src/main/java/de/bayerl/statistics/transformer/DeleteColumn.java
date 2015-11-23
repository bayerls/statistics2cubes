package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Created by sebastianbayerl on 02/06/15.
 */
public class DeleteColumn extends Transformation {

    private int index;

    public DeleteColumn(int index) {
        this.index = index;
    }

    @Override
    public String getName() {
        return "deleteColumn";
    }

    @Override
    protected Table transformStep(Table table) {

        for (Row row : table.getRows()) {
            row.getCells().remove(index);
        }

        return table;
    }
}
