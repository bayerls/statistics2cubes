package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.Cell;

/**
 * Created by sebastianbayerl on 08/04/15.
 */
public class ResolveWrongLabels extends Transformation {

    private int columnOffset;

    public ResolveWrongLabels(@NameAnnotation(name = "column offset") int columnOffset) {
        this.columnOffset = columnOffset;
    }

    @Override
    public String getName() {
        return "resolveWrongLabels";
    }

    @Override
    protected Table transformStep(Table table) {

        for (Row row : table.getRows()) {
            boolean hasData = false;
            for (int i = columnOffset; i < row.getCells().size(); i++) {
                Cell cell = row.getCells().get(i);
                // check if there is a data cell in this row
                if (cell.getRole().equals("data")) {
                    hasData = true;
                    break;
                }
            }

            if (hasData) {
                // set every cell to data after the offset
                for (int i = columnOffset; i < row.getCells().size(); i++) {
                    Cell cell = row.getCells().get(i);
                    cell.setRole("data");
                }
            }
        }

        return table;
    }
}
