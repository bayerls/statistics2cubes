package de.bayerl.statistics.transformer;


import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

/**
 * Ensures, that the fact column can be cast to double values. It at least one cell fails this conversion a new
 * column is introduced, containing all parts of the cell, which cannot be casted.
 */
public class CleanFacts extends Transformation {

    @Override
    public String getName() {
        return "cleanFacts";
    }

    @Override
    protected Table transformStep(Table table) {

        // check if necessary
        boolean transformationNecessary = false;
        try {
            for (Row row : table.getRows()) {
                for (Cell cell : row.getCells()) {
                    Double.parseDouble(cell.getValue().getValue());
                }
            }
        } catch (NumberFormatException e) {
            transformationNecessary = true;
        }

        if (transformationNecessary) {
            Cell newCell = new Cell();
            newCell.setRole("label");
            newCell.getValue().setValue("-");
            AddColumn addColumn = new AddColumn(table.getRows().get(0).getCells().size(), newCell);
            table = addColumn.transform(table);
            DeleteRowColNumbers deleteRowColNumbers = new DeleteRowColNumbers();
            table = deleteRowColNumbers.transform(table);

            for (Row row : table.getRows()) {
                // Simplification: assume only one fact column with index 0
                Cell cell = row.getCells().get(0);
                Cell lastCell = row.getCells().get(row.getCells().size() - 1);
                String value = cell.getValue().getValue();

                String newValue = "";
                String info = "";

                if (value.equals("─")) {
                    newValue = "0";
                    info = "unknown value";
                } else {
                    String[] splits = value.split("\\s");
                    double parsed = 0;

                    for (String split : splits) {
                        try {
                            parsed = Double.parseDouble(split);

                        } catch (NumberFormatException e) {
                            info += split + " ";
                        }
                        newValue = "" + parsed;
                        newValue = newValue.replace(".0", "");
                    }

                }

                if (!info.equals("")) {
                    cell.getValue().setValue(newValue);
                    lastCell.getValue().setValue(info);
                }
            }
        }

        return table;
    }
}
