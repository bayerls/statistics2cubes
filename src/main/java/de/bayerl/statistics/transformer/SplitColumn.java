package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.Cell;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Splits a column based on a regex expression.
 */
public class SplitColumn extends Transformation {

    private String regex;
    private int colNumber;

    public SplitColumn(@NameAnnotation(name = "splitter") String regex, @NameAnnotation(name = "col") int colNumber) {
        this.regex = regex;
        this.colNumber = colNumber;
    }

    @Override
    public String getName() {
        return "splitRow_" + regex + "_" + colNumber;
    }

    @Override
    protected Table transformStep(Table table) {
        DeepCopyUtil deepCopyUtil = new DeepCopyUtil();

        for (Row row : table.getRows()) {
            Cell cell = row.getCells().get(colNumber);
            String[] splits = cell.getValue().getValue().split(regex);
            Cell c = row.getCells().remove(colNumber);

            List<String> splitValues = Arrays.asList(splits);
            Collections.reverse(splitValues);

            for (String value : splitValues) {
                c = deepCopyUtil.deepCopy(c);
                c.getValue().setValue(value);
                row.getCells().add(colNumber, c);
            }
        }

        return table;
    }
}
