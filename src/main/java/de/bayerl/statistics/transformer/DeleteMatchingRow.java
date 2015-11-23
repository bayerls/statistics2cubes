package de.bayerl.statistics.transformer;

import com.google.common.collect.Lists;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.TableSliceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Checks if a value in a row matches the given term. If true the complete row is deleted.
 */
public class DeleteMatchingRow extends Transformation {

    private String term;
    private int[] protectedRows;
    private boolean exact = false;

    public DeleteMatchingRow(@NameAnnotation(name = "term") String term, @NameAnnotation(name = "protected") int[] protectedRows, @NameAnnotation(name = "exact") boolean exact) {
        this.term = term;
        this.protectedRows = protectedRows;
        this.exact = exact;
    }



    @Override
    public String getName() {
        return "deleteMatchingRow_" + term;
    }

    @Override
    protected Table transformStep(Table table) {
        List<Integer> rows = new ArrayList<>();
        // find rows that will be deleted
        for (int i = 0; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);

            for (int y = 0; y < row.getCells().size(); y++) {
                Cell cell = row.getCells().get(y);
                // check if the current value contains the term

                if (exact) {
                    if (cell.getValue().getValue().equals(term)) {
                        rows.add(i);
                        break;
                    }
                } else {
                    if (cell.getValue().getValue().contains(term)) {
                        rows.add(i);
                        break;
                    }
                }
            }
        }

        // check for protected rows
        for (int i : protectedRows) {
            for (int y = 0; y < rows.size(); y++) {
                if (rows.get(y) == i) {
                    rows.remove(y);
                    break;
                }
            }
        }

        // prepare row list for deletion: sort descending
        List<Integer> copy = Lists.newArrayList(rows);
        Collections.sort(copy);
        Collections.reverse(copy);

        // delete rows
        for (Integer i : copy) {
            DeleteRowCol deleteRowCol = new DeleteRowCol(TableSliceType.ROW, i);
            table = deleteRowCol.transformStep(table);
        }

        return table;
    }
}
