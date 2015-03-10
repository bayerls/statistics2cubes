package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

public class ReplaceValue extends Transformation {

    private String target;
    private String replacement;

    public ReplaceValue(String target, String replacement) {
        this.target = target;
        this.replacement = replacement;
    }

    @Override
    public String getName() {
        return "replaceValue";
    }

    @Override
    protected Table transformStep(Table table) {

        for (Row row : table.getRows()) {
            for (Cell cell : row.getCells()) {
                String value = cell.getValue().getValue();
                value = value.replaceAll(target, replacement);
                cell.getValue().setValue(value);
            }
        }

        return table;
    }
}
