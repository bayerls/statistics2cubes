package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.Random;

/**
 * Created by sebastianbayerl on 24/03/15.
 */
public class SetValueColumn extends Transformation {
    private int colNumber;

    public SetValueColumn(int colNumber) {
        this.colNumber = colNumber;
    }

    @Override
    public String getName() {
        return "setValueColumn_random";
    }

    @Override
    protected Table transformStep(Table table) {
        Random r = new Random();
        for (Row row : table.getRows()) {
            row.getCells().get(colNumber).getValue().setValue("" + r.nextInt(1000));
        }

        return table;
    }
}
