package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Finds empty cells and prints their coordinates.
 */
public class SanityNotEmpty extends Transformation{
    @Override
    public String getName() {
        return "sanityNotEmpty";
    }

    @Override
    protected Table transformStep(Table table) {

        for (int i = 0; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);

            for (int y = 0; y < row.getCells().size(); y++) {
                Cell cell = row.getCells().get(y);
                if (cell.getValue().getValue() == null || cell.getValue().getValue().equals("")) {
                    System.out.println("Empty: " + i + " / " + y);
                }
            }
        }

        return table;
    }
}
