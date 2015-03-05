package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Normalizes tables that are compounded with labelUnit rows.
 */
public class NormalizeCompoundTables extends Transformation {

    @Override
    public String getName() {
        return "normalizeCompoundTables";
    }

    @Override
    protected Table transformStep(Table table) {
        List<Table> splitTables = new ArrayList<>();
        Table tempTable = new Table();
        List<Row> headers = new ArrayList<>();
        boolean headersDone = false;
        for (Row row : table.getRows()) {
            // detect fixed header
            if (!headersDone) {
                if (row.getCells().get(row.getCells().size() - 1).getRole().equals("label")) {
                    headers.add(row);
                } else {
                    headersDone = true;
                }
            }

            // split the compound tables
            if (headersDone) {
                if (row.getCells().get(row.getCells().size() - 1).getRole().equals("labelUnit")) {
                    splitTables.add(tempTable);
                    tempTable = new Table();
                    tempTable.getRows().addAll(headers);
                }
                tempTable.getRows().add(row);
            }
        }

        // remove first table (it is empty)
        splitTables.remove(0);

        // normalize the list of tables
        List<Table> normalizedTables = new ArrayList<>();
        for (Table t : splitTables) {
            NormalizeTable normalizeTable = new NormalizeTable();
            normalizedTables.add(normalizeTable.transformStep(t));
        }

        // merge tables
        Table mergedTable = new Table();
        for (Table t : normalizedTables) {
            mergedTable.getRows().addAll(t.getRows());
        }

        return mergedTable;
    }

}
