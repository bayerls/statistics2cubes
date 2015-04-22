package de.bayerl.statistics.transformer;

import com.google.common.collect.Lists;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.TableSliceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sebastianbayerl on 08/04/15.
 */
public class DeleteRowByType extends Transformation {

    private String type;

    public DeleteRowByType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return "deleteRowByType_" + type;
    }

    @Override
    protected Table transformStep(Table table) {
        List<Integer> rows = new ArrayList<>();
        // find rows that will be deleted
        for (int i = 0; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);

            if (row.getCells().get(0).getRole().equals(type)) {
                rows.add(i);
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
