package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.List;

/**
 * Created by sebastianbayerl on 02/06/15.
 */
public class ExtractColumns extends Transformation {

    private int fromIndex;
    private int toIndex;

    public ExtractColumns(int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public String getName() {
        return "deleteColumnsProtected";
    }

    @Override
    protected Table transformStep(Table table) {

        for (Row row : table.getRows()) {
            List<Cell> cells = row.getCells();
            row.setCells(cells.subList(fromIndex, toIndex));
        }

        return table;
    }
}
