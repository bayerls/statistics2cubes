package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.TableSliceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Combine multiple rows into a single one.
 */
public class CombineRows extends Transformation {

    private final static String DIVIDER = " - ";
    private int[] rows;

    public CombineRows(@NameAnnotation(name = "rows") int[] rows) {
        this.rows = rows;
    }

    @Override
    public String getName() {
        String name = "combineRows";

        for (Integer i : rows) {
            name += "_" + i;
        }

        return name;
    }

    @Override
    protected Table transformStep(Table table) {
        int targetRow = rows[0];

        // combine pair wise
        for (int i = 1; i < rows.length; i++) {
            for (int y = 0; y < table.getRows().get(i).getCells().size(); y++) {
                Cell targetCell = table.getRows().get(targetRow).getCells().get(y);
                Cell cell = table.getRows().get(rows[i]).getCells().get(y);
                // append cell content to targetCell
                targetCell.getValue().setValue(targetCell.getValue().getValue() + DIVIDER + cell.getValue().getValue());
            }
        }

        // prepare row list for deletion
        int[] copy = Arrays.copyOf(rows, rows.length);
        Arrays.sort(copy);
        List<Integer> list = new ArrayList<>();
        for (int i : copy) {
            list.add(i);
        }
        Collections.reverse(list);

        // delete unneeded rows
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != targetRow) {
                DeleteRowCol deleteRowCol = new DeleteRowCol(TableSliceType.ROW, list.get(i));
                table = deleteRowCol.transformStep(table);
            }
        }

        return table;
    }
}
