package de.bayerl.statistics.instance;

import de.bayerl.statistics.model.TableSliceType;
import de.bayerl.statistics.transformer.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianbayerl on 08/04/15.
 */
public class Example3 implements Conversion {

    // table_178-drsa_380_0240_48.tei.de.bayerl.statistics.gui.html

    @Override
    public String getFolder() {
        return "3";
    }

    @Override
    public List<Transformation> getTransformations() {
        List<Transformation> transformations = new ArrayList<>();

        transformations.add(new ResolveLinebreaks());
        transformations.add(new ResolveRowSpan());
        transformations.add(new ResolveColSpan());

        int[] protectedRows = {};
        transformations.add(new DeleteMatchingRow("Noch: ", protectedRows, false));
        transformations.add(new DeleteMatchingRow("Summe ", protectedRows, false));

        transformations.add(new DeleteRowByType("labelOrd"));

        transformations.add(new DeleteMatchingRow("Seeverkehr in den Deutschen Hafenplätzen für das Jahr 1878.", protectedRows, false));
        transformations.add(new DeleteRowCol(TableSliceType.ROW, 0));

        transformations.add(new ResolveWrongLabels(2));


        int[] protectedHeader = {0,1,2};
        transformations.add(new DeleteMatchingRow("Länder bezw. Küstenstrecken der Herkunft und Bestimmung.", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("(Fortsetzung.)", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("den Ländern (Küstenstrecken) der Herkunft und Bestimmung und nach den Flaggen.", protectedHeader, false));







        // Title: Seeverkehr in den Deutschen Hafenplätzen für das Jahr 1878.
        // Description: III. Uebersicht der im Seeverkehr angekommenen und abgegangenen Schiffe nach den Ländern (Küstenstrecken) der Herkunft und Bestimmung und nach den Flaggen.


        return transformations;
    }

}