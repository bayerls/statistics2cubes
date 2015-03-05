package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Normalizes the table into the row format: fact, dimension, dimension, dimension, ...
 */
public class NormalizeTable extends Transformation {

    private int topDims = 0;
    private int leftDims = 0;

    @Override
    public String getName() {
        return "normalizeTable";
    }

    @Override
    protected Table transformStep(Table table) {

        // detect dimensions
        for (Cell cell : table.getRows().get(table.getRows().size() - 1).getCells()) {
            if (cell.getRole().equals("data")) {
                break;
            } else {
                leftDims++;
            }
        }

        for (int i = 0; i < table.getRows().size(); i++) {
            List<Cell> cells = table.getRows().get(i).getCells();
            if (cells.get(cells.size() - 1).getRole().equals("data")) {
                break;
            } else {
                topDims++;
            }
        }

        // get normalized cell matrix
        List<List<Cell>> normalized = new ArrayList<>();
        normalized.add(getFacts(table));
        normalized.addAll(getTopDims(table));
        normalized.addAll(getLeftDims(table));

        // transpose matrix and generate new table
        Table resultTable = new Table();

        for (int i = 0; i < normalized.get(0).size(); i++) {
            Row row = new Row();
            resultTable.getRows().add(row);

            for (int y = 0; y < normalized.size(); y++) {
                List<Cell> cells = normalized.get(y);
                row.getCells().add(cells.get(i));
            }
        }

        return resultTable;
    }

    private List<Cell> getFacts(Table table) {
        List<Cell> facts = new ArrayList<>();

        // iterate rows
        for (int i = topDims; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);

            // iterate cells
            for (int y = leftDims; y < row.getCells().size(); y++) {
                facts.add(row.getCells().get(y));
            }
        }

        return facts;
    }

    private List<List<Cell>> getTopDims(Table table) {
        List<List<Cell>> dimensions = new ArrayList<>();

        for (int i = 0; i < topDims; i++) {
            Row row = table.getRows().get(i);
            List<Cell> dim = new ArrayList<>();
            for (int y = leftDims; y < row.getCells().size(); y++) {
                dim.add(row.getCells().get(y));
            }

            List<Cell> expandedDim = new ArrayList<>();
            for (int k = 0; k < table.getRows().size() - topDims; k++) {
                expandedDim.addAll(dim);
            }

            dimensions.add(expandedDim);
        }

        return dimensions;
    }

    private List<List<Cell>> getLeftDims(Table table) {
        List<List<Cell>> dimensions = new ArrayList<>();

        for (int i = 0; i < leftDims; i++) {
            List<Cell> dim = new ArrayList<>();
            dimensions.add(dim);

            for (int y = topDims; y < table.getRows().size(); y++) {
                Cell cell = table.getRows().get(y).getCells().get(i);

                for (int k = 0; k < table.getRows().get(0).getCells().size() - leftDims; k++) {
                    dim.add(cell);
                }
            }
        }

        return dimensions;
    }
}
