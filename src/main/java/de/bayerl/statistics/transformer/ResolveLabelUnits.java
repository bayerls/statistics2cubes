package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 *  Resolves wrongly labeled roles. (label -> labelUnit)
 */
public class ResolveLabelUnits extends Transformation {
    @Override
    public String getName() {
        return "resolveLabelUnit";
    }

    @Override
    protected Table transformStep(Table table) {
        // find rows with wrong roles
        List<Integer> rows = new ArrayList<>();

        for (int i = 0; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);

            if (row.getCells().get(0).getRole().equals("labelOrd")) {
                boolean candidateRow = true;
                for (int y = 1; y < row.getCells().size(); y++) {
                    if (!row.getCells().get(y).getRole().equals("label")) {
                        candidateRow = false;
                        break;
                    }
                }

                if (candidateRow) {
                    rows.add(i);
                }
            }
        }

        // set the correct roles
        for (Integer rowNumber : rows) {
            Row row = table.getRows().get(rowNumber);

            for (int i = 1; i < row.getCells().size(); i++) {
                SetType setType = new SetType("labelUnit", rowNumber, i);
                table = setType.transformStep(table);
            }
        }

        return table;
    }

}
