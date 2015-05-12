package de.bayerl.statistics.transformer;

import de.bayerl.statistics.converter.vocabulary.CODE;
import de.bayerl.statistics.converter.vocabulary.Data42;
import de.bayerl.statistics.converter.vocabulary.LocalNS;
import de.bayerl.statistics.converter.vocabulary.VA;
import de.bayerl.statistics.model.Header;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.instance.Config;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;

/**
 * Creates the headers for the columns. Apply to normalized table. Necessary for triplification.
 */
public class CreateHeaders extends Transformation {

    private String[] headerLabels;

    public CreateHeaders(@NameAnnotation(name = "headers") String[] headerLabels) {
        this.headerLabels = headerLabels;
    }

    @Override
    public String getName() {
        return "createHeaders";
    }

    @Override
    protected Table transformStep(Table table) {
        LocalNS localNS;
        if (Config.GENERATE_1_2) {
            localNS = new Data42();
        } else {
            localNS = new CODE();
        }

        Row row = table.getRows().get(0);
        for (int i = 0; i < headerLabels.length; i++) {
            Cell cell = row.getCells().get(i);
            Header header = new Header();
            header.setLabel(headerLabels[i]);
            header.setUrl(localNS.COMPONENT + "-" + i);
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
