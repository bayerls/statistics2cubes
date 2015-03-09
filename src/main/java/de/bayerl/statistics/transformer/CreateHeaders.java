package de.bayerl.statistics.transformer;

import de.bayerl.statistics.converter.vocabulary.Data42;
import de.bayerl.statistics.converter.vocabulary.VA;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Header;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

public class CreateHeaders extends Transformation {

    private String[] headerLabels;

    public CreateHeaders(String[] headerLabels) {
        this.headerLabels = headerLabels;
    }

    @Override
    public String getName() {
        return "createHeaders";
    }

    @Override
    protected Table transformStep(Table table) {
        Row row = table.getRows().get(0);
        for (int i = 0; i < row.getCells().size(); i++) {
            Cell cell = row.getCells().get(i);
            Header header = new Header();
            header.setLabel(headerLabels[i]);
            header.setUrl(Data42.COMPONENT + "-" + i);
            String range = VA.getURI() + "cubeDimensionNominal";

            if (cell.getRole().equals("data")) {
                range = VA.getURI() + "cubeObservationText";
            }
            header.setRange(range);
            table.getHeaders().add(header);
        }

        return table;
    }
}
