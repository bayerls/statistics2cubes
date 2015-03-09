package de.bayerl.statistics.instance;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.transformer.*;

import java.util.ArrayList;
import java.util.List;

public class Example2 implements Conversion {


    @Override
    public String getFolder() {
        return "2";
    }

    @Override
    public List<Transformation> getTransformations() {
        List<Transformation> transformations = new ArrayList<>();

        transformations.add(new ResolveLinebreaks());
        transformations.add(new ResolveRowSpan());
        transformations.add(new ResolveColSpan());

        transformations.add(new DeleteRowCol(true, 1));
        int[] rows = {0, 1};
        transformations.add(new DeleteMatchingRow("Niederlage-Verkehr des Deutschen", rows));
        transformations.add(new DeleteMatchingRow("Im Ganzen", rows));

        transformations.add(new DeleteMatchingRow("Ctr.", rows));
        transformations.add(new DeleteMatchingRow("Tonnen", rows));

        Cell cell = new Cell();
        cell.setRole("label");
        cell.getValue().setValue("Ctr.");
        transformations.add(new AddColumn(0, cell));
        transformations.add(new SetValueIntervalColumn(0, 234, 241, "Tonnen"));

        transformations.add(new DeleteMatchingRow("Bestand zu Anfang des Quartals", rows));

        transformations.add(new NormalizeCompoundTables("label", 2));
        transformations.add(new TrimValues());
        transformations.add(new SanityNotEmpty());

        String[] headerLabels = {"Anzahl", "Kategorie", "Bestand (Änderung)", "Ware", "Nummern der Waarenverzeichnisse resp. Tarifposition", "Gebiet"};
        transformations.add(new CreateHeaders(headerLabels));
        transformations.add(new AddMetadata("Niederlage-Verkehr des Deutschen Zollgebiets",
                "Niederlage-Verkehr des Deutschen Zollgebiets mit den wichtigeren Niederlagegütern im ersten Quartal 1873",
                "https://github.com/bayerls"));

        return transformations;
    }
}
