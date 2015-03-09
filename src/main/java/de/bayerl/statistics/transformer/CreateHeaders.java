package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Header;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

public class CreateHeaders extends Transformation {

    @Override
    public String getName() {
        return "createHeaders";
    }

    @Override
    protected Table transformStep(Table table) {
        // TODO do this right

        Row row = table.getRows().get(0);
        for (int i = 0; i < row.getCells().size(); i++) {
//            Cell cell = row.getCells().get(i);
            Header header = new Header();
            header.setLabel("label_" + i);
            header.setUrl("http://www.test.de/url-" + i);
            table.getHeaders().add(header);
        }

        return table;
    }
}
