package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Introduce redundancy to resolve column spans
 */
public class ResolveColSpan extends Transformation {

    @Override
    public String getName() {
        return "resolveColSpan";
    }

    @Override
    public Table transformStep(Table table) {
        DeepCopyUtil dcUtil = new DeepCopyUtil();

        for (int i = 0; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);

            for (int y = 0; y < row.getCells().size(); y++) {
                Cell cell = row.getCells().get(y);
                int rows = cell.getRows();
                cell.setRows(0);
                rows--;

                while (rows > 0) {
                    Cell addCell = dcUtil.deepCopy(cell);
                    Row addAtRow = table.getRows().get(i + rows);

                    if (addAtRow.getCells().size() == y) {
                        addAtRow.getCells().add(addCell);
                    } else {
                        addAtRow.getCells().add(y, addCell);
                    }

                    rows--;
                }
            }
        }

        return table;
    }
}
