package de.bayerl.statistics.transformer;

import com.google.common.collect.Lists;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.*;

/**
 * Created by sebastianbayerl on 02/06/15.
 */
public class CombineLabelRows extends Transformation {

    private int[] headers;

    public CombineLabelRows(int[] headers) {
        this.headers = headers;
    }

    @Override
    public String getName() {
        return "combineLabelRows";
    }

    @Override
    protected Table transformStep(Table table) {
        String lastRole = "data";
        int labelStart = 0;
        int labelSize = 0;

        Map<Integer, Integer> labelRows = new HashMap<>();

        for (int i = headers[headers.length - 1] + 1; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);
            String currentRole = row.getCells().get(row.getCells().size() - 1).getRole();

            if (lastRole.equals("data") && currentRole.equals("label")) {
                labelStart = i;
                labelSize = 1;
            } else if (lastRole.equals("label") && currentRole.equals("label")) {
                labelSize++;
            } else if (lastRole.equals("label") && currentRole.equals("data")) {
                if (labelSize > 1) {
                    labelRows.put(labelStart, labelSize);
                }
            }

            lastRole = currentRole;
        }

        List<Integer> keys = Lists.newArrayList(labelRows.keySet());
        Collections.sort(keys);
        Collections.reverse(keys);

        for (Integer key : keys) {

            int[] rows = new int[labelRows.get(key)];

            for (int i = 0; i < labelRows.get(key); i++) {
                rows[i] = key + i;
            }

            Transformation transformation = new CombineRows(rows);
            table = transformation.transform(table);
        }

        return table;
    }
}
