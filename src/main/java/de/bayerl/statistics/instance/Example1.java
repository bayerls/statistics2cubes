package de.bayerl.statistics.instance;

import de.bayerl.statistics.transformer.*;

import java.util.ArrayList;
import java.util.List;

public class Example1 implements Conversion {

    @Override
    public String getFolder() {
        return "1/";
    }

    @Override
    public List<Transformation> getTransformations() {
        // Prepare transformations
        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new ResolveLinebreaks());
        transformations.add(new ResolveRowSpan());
        transformations.add(new ResolveColSpan());

        transformations.add(new DeleteRowCol(false, 5));
        int[] rows = {0, 1, 2};
        transformations.add(new DeleteMatchingRow("den freien Verkehr", rows));
        transformations.add(new DeleteMatchingRow("Nummern der Waarenverzeichnisse", rows));
        transformations.add(new DeleteMatchingRow("Die erste, laufende Nummer bezieht sich", rows));
        transformations.add(new DeleteMatchingRow("Zusammen Ctr.", rows));
        transformations.add(new DeleteMatchingRow("Soweit sie nicht unter", rows));
        transformations.add(new DeleteMatchingRow("Mit Ausn. der unter", rows));


        transformations.add(new SetValue("31. (274.) Pos. 5 a.", 239, 0));
        transformations.add(new SetValue("35. (218.) Pos. 5 c.", 268, 0));

        transformations.add(new SetType("data", 410, 4));
        transformations.add(new SetType("data", 268, 2));
        transformations.add(new SetType("data", 476, 4));
        transformations.add(new SetType("data", 628, 4));

        transformations.add(new DeleteRowCol(false, 4));
        transformations.add(new DeleteRowCol(true, 816));
        transformations.add(new DeleteRowCol(true, 815));

        transformations.add(new DeleteMatchingRow("Zusammen a. Tonn.", rows));
        transformations.add(new DeleteMatchingRow("b. Kubikmeter", rows));
        transformations.add(new DeleteMatchingRow("Zusammen a. Ctr.", rows));
        transformations.add(new DeleteMatchingRow("b. Hektoliter", rows));
        transformations.add(new DeleteMatchingRow("c. Kubikmeter", rows));
        transformations.add(new DeleteMatchingRow("c. Stück", rows));
        transformations.add(new DeleteMatchingRow("b. Stck.", rows));
        transformations.add(new SetValue("Berechneter Zollbetrag, Thlr.", 2348, 1));
        transformations.add(new SetValue("426. (80.) Pos. 39 b.", 3632, 0));
        transformations.add(new ResolveLabelUnits());
        transformations.add(new ResolveColumnTypes());

        transformations.add(new SanityNotEmpty());

        transformations.add(new NormalizeCompoundTables("labelUnit"));

        // after normalization
        transformations.add(new TrimValues());
        String[] headerLabels = {"Anzahl", "Kategorie", "Einfuhr oder Ausfuhr", "Quartal", "Ware", "Nummern der Waarenverzeichnisse resp. Tarifposition", "Gebiet"};
        transformations.add(new CreateHeaders(headerLabels));
        transformations.add(new AddMetadata("In den freien Verkehr des Deutschen Zollgebiets getretene Waaren",
                "In den freien Verkehr des Deutschen Zollgebiets getretene Waaren; Einfuhr; 1. Quartal 1873 und 1872",
                "https://github.com/bayerls"));

        return transformations;
    }

}
