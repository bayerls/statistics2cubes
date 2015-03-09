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
            String value1 = row.getCells().get(0).getValue().getValue();
            String value2 = row.getCells().get(1).getValue().getValue();

            boolean candidateRow = true;

            for (int y = 2; y < row.getCells().size(); y++) {
                String value = row.getCells().get(y).getValue().getValue();
                if (!(!value.equals(value1) && value.equals(value2))) {
                    candidateRow = false;
                    break;
                }
            }

            if (candidateRow) {
                rows.add(i);
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
