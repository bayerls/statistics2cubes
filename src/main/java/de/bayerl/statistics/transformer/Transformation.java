package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Table;

/**
 * Transformations can be applied to tables. Row numbers are removed before transformation and again added afterwards.
 */
public abstract class Transformation {

    public abstract String getName();

    public Table transform(Table table) {
        DeleteRowColNumbers deleteRowColNumbers = new DeleteRowColNumbers();
        AddRowColNumbers addRowColNumbers = new AddRowColNumbers();

        if (table.isNumbered()) {
            table = deleteRowColNumbers.transform(table);
        }

        // do the actual transformation step
        table = transformStep(table);

        if (!table.isNumbered()) {
            table = addRowColNumbers.transform(table);
        }

        return table;
    }

    protected abstract Table transformStep(Table table);

}
