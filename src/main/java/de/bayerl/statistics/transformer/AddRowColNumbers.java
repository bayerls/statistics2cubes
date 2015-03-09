package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumerates columns and rows.
 */
public class AddRowColNumbers implements MetaTransformation {

    @Override
    public Table transform(Table table) {
        table.setNumbered(true);
        List<Cell> cells = new ArrayList<>();

        int cellNumber = 0;

        // compute total number of cells
        for (Cell cell : table.getRows().get(0).getCells()) {
            if (cell.getCols() > 0) {
                cellNumber += cell.getCols();
            } else {
                cellNumber++;
            }
        }

        // start 0 based
        for (int i = 0; i < cellNumber; i++) {
            Cell cell = new Cell();
            cell.getValue().setValue("" + i);
            cells.add(cell);
        }

        Row row = new Row();
        row.getCells().addAll(cells);
        table.getRows().add(0, row);

        // -1 for the top left cell
        for (int i = 0; i < table.getRows().size(); i++) {
            Row currentRow = table.getRows().get(i);
            Cell cell = new Cell();
            cell.getValue().setValue("" + (i - 1));
            currentRow.getCells().add(0, cell);
        }

        return table;
    }
}
