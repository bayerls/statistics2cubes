package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Normalizes compounded tables.
 */
public class NormalizeCompoundTables extends Transformation {

    private String splitOnType;
    private int fixedHeaderSize = -1;

    public NormalizeCompoundTables(String splitOnType, int fixedHeaderSize) {
        this.splitOnType = splitOnType;
        this.fixedHeaderSize = fixedHeaderSize;
    }

    public NormalizeCompoundTables(String splitOnType) {
        this.splitOnType = splitOnType;
    }


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
        int rowCount = 0;
        for (Row row : table.getRows()) {
            // detect fixed header
            if (!headersDone) {
                if (fixedHeaderSize > -1) {
                    if (rowCount < fixedHeaderSize) {
                        headers.add(row);
                    } else {
                        headersDone = true;
                    }
                } else {
                    if (row.getCells().get(row.getCells().size() - 1).getRole().equals("label")) {
                        headers.add(row);
                    } else {
                        headersDone = true;
                    }
                }

                rowCount++;
            }

            // split the compound tables
            if (headersDone) {
                if (row.getCells().get(row.getCells().size() - 1).getRole().equals(splitOnType)) {
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
