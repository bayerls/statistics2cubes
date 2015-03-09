package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

public class AddColumn extends Transformation {

    private int colNumber;
    private Cell cell;

    public AddColumn(int colNumber, Cell cell) {
        this.colNumber = colNumber;
        this.cell = cell;
    }

    @Override
    public String getName() {
        return "addColumn";
    }

    @Override
    protected Table transformStep(Table table) {
        DeepCopyUtil deepCopyUtil = new DeepCopyUtil();
        for (Row row : table.getRows()) {
            row.getCells().add(colNumber, deepCopyUtil.deepCopy(cell));
        }

        return table;
    }
}
