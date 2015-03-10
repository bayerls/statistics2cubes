package de.bayerl.statistics.instance;

import de.bayerl.statistics.model.TableSliceType;
import de.bayerl.statistics.transformer.*;

import java.util.ArrayList;
import java.util.List;

public class Example1 implements Conversion {

    @Override
    public String getFolder() {
        return "1";
    }

    @Override
    public List<Transformation> getTransformations() {
        // Prepare transformations
        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new ResolveLinebreaks());
        transformations.add(new ResolveRowSpan());
        transformations.add(new ResolveColSpan());

        transformations.add(new DeleteRowCol(TableSliceType.COLUMN, 5));
        int[] protectedRows = {0, 1, 2};
        transformations.add(new DeleteMatchingRow("den freien Verkehr", protectedRows));
        transformations.add(new DeleteMatchingRow("Nummern der Waarenverzeichnisse", protectedRows));
        transformations.add(new DeleteMatchingRow("Die erste, laufende Nummer bezieht sich", protectedRows));
        transformations.add(new DeleteMatchingRow("Zusammen Ctr.", protectedRows));
        transformations.add(new DeleteMatchingRow("Soweit sie nicht unter", protectedRows));
        transformations.add(new DeleteMatchingRow("Mit Ausn. der unter", protectedRows));


        transformations.add(new SetValue("31. (274.) Pos. 5 a.", 239, 0));
        transformations.add(new SetValue("35. (218.) Pos. 5 c.", 268, 0));

        transformations.add(new SetType("data", 410, 4));
        transformations.add(new SetType("data", 268, 2));
        transformations.add(new SetType("data", 476, 4));
        transformations.add(new SetType("data", 628, 4));

        transformations.add(new DeleteRowCol(TableSliceType.COLUMN, 4));
        transformations.add(new DeleteRowCol(TableSliceType.ROW, 816));
        transformations.add(new DeleteRowCol(TableSliceType.ROW, 815));

        transformations.add(new DeleteMatchingRow("Zusammen a. Tonn.", protectedRows));
        transformations.add(new DeleteMatchingRow("b. Kubikmeter", protectedRows));
        transformations.add(new DeleteMatchingRow("Zusammen a. Ctr.", protectedRows));
        transformations.add(new DeleteMatchingRow("b. Hektoliter", protectedRows));
        transformations.add(new DeleteMatchingRow("c. Kubikmeter", protectedRows));
        transformations.add(new DeleteMatchingRow("c. Stück", protectedRows));
        transformations.add(new DeleteMatchingRow("b. Stck.", protectedRows));
        transformations.add(new SetValue("Berechneter Zollbetrag, Thlr.", 2348, 1));
        transformations.add(new SetValue("426. (80.) Pos. 39 b.", 3632, 0));
        transformations.add(new ResolveLabelUnits());
        transformations.add(new ResolveColumnTypes());

        transformations.add(new SanityNotEmpty());

        transformations.add(new NormalizeCompoundTables("labelUnit"));

        // after normalization
        transformations.add(new ReplaceValue("†", "† "));
        transformations.add(new CleanFacts());
        transformations.add(new TrimValues());
        String[] headerLabels = {"Anzahl", "Kategorie", "Einfuhr oder Ausfuhr", "Quartal", "Ware", "Nummern der Waarenverzeichnisse resp. Tarifposition", "Gebiet", "Fakten Info"};
        transformations.add(new CreateHeaders(headerLabels));
        transformations.add(new AddMetadata("In den freien Verkehr des Deutschen Zollgebiets getretene Waaren",
                "In den freien Verkehr des Deutschen Zollgebiets getretene Waaren; Einfuhr; 1. Quartal 1873 und 1872",
                "https://github.com/bayerls"));



        return transformations;
    }

}
