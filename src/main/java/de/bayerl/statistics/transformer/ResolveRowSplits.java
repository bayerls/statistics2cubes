package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianbayerl on 01/06/15.
 */
public class ResolveRowSplits extends Transformation {
    @Override
    public String getName() {
        return "resolveRowSplits";
    }

    @Override
    protected Table transformStep(Table table) {

        //System.out.println(table.getRows().get(4).getCells().get(0).getValue().getValue());

        int lastSize = 0;
        boolean concat = true;
        List<Row> rows = new ArrayList<>();
        List<Integer> rowNumbers = new ArrayList<>();
        int concatRow = 0;
        boolean skipedFirst = false;

        for (int i = 0; i < table.getRows().size(); i++) {
            int size = table.getRows().get(i).getCells().size();
//
            if (lastSize != size) {
                //System.out.println(table.getRows().get(i).getCells().size());
                lastSize = size;
                concat = !concat;


// TODO here is a bug?
                if (rows.size() > 1 && skipedFirst) {
                    if (rows.get(rows.size() - 1) != rows.get(rows.size() - 2)) {
                        concatRow++;
                    }

                }

                skipedFirst = true;

                rowNumbers.add(i);
            }

            if (concat) {
                //System.out.println(concatRow);
//                if (rows.size() > concatRow) {
//                    rows.get(concatRow).getCells().addAll(table.getRows().get(i).getCells());
//                } else {
//                    rows.add(table.getRows().get(i));
//                    System.out.println("error " + concatRow);
//                }
//                concatRow++;
            } else {
                rows.add(table.getRows().get(i));
            }



        }
//
//        for (int i = 1; i < rowNumbers.size() - 1; i = i + 2) {
//            int zero = rowNumbers.get(i - 1);
//            int first = rowNumbers.get(i);
//            int second = rowNumbers.get(i + 1);
//
//            int a = (first - zero);
//            int b = (second - first);
//
//            if (a < b) {
//                System.out.println(first + "   " + a + "    " + b + "   " + (a == b));
//
//            }


//        }


        table.setRows(rows);




        return table;
    }
}
