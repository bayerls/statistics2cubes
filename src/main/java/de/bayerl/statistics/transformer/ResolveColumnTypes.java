package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Detects and resolves wrongly set roles. CAUTION: Implementation is depending on input tables layout!
 */
public class ResolveColumnTypes extends Transformation {

    @Override
    public String getName() {
        return "resolveColumnType";
    }

    @Override
    protected Table transformStep(Table table) {
        // set invalid cells to "data"
        for (int i = 3; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);

            for (int y = 2; y < row.getCells().size(); y++) {
                Cell cell = row.getCells().get(y);
                String type = cell.getRole();
                if (!type.equals("data") && !type.equals("labelUnit")) {
                    cell.setRole("data");
                }
            }
        }

        return table;
    }
}
