package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Introduce redundancy to resolve row spans
 */
public class ResolveRowSpan extends Transformation {

    @Override
    public String getName() {
        return "resolveRowSpan";
    }

    @Override
    public Table transformStep(Table table) {
        DeepCopyUtil dcUtil = new DeepCopyUtil();

        for (Row row : table.getRows()) {
            for (int i = 0; i < row.getCells().size(); i++) {
                Cell cell = row.getCells().get(i);
                int cols = cell.getCols();
                cell.setCols(0);
                cols--;

                while (cols > 0) {
                    Cell addCell = dcUtil.deepCopy(cell);
                    row.getCells().add(i, addCell);
                    cols--;
                }
            }
        }

        return table;
    }

}
