package de.bayerl.statistics.instance;

import de.bayerl.statistics.transformer.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianbayerl on 02/06/15.
 */
public class Example5 implements Conversion {
    @Override
    public String getFolder() {
        return "5";
    }

    @Override
    public List<Transformation> getTransformations() {
        List<Transformation> transformations = new ArrayList<>();


        transformations.add(new ResolveLinebreaks());
        transformations.add(new ResolveRowSpan());
        transformations.add(new ResolveColSpan());

        // review

        transformations.add(new ResolveRowSplits());
        int[] protectedHeader = {0,1,2};
        transformations.add(new DeleteMatchingRow("Anmerkung. Wegen der mit kleinen Ziffern unter der Linie aufgeführten Zahlen siehe Bemerkung 3 auf Seite 1.", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("Zusammen", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("Uebersicht sämmtlicher am 1. Dezember 1875 erhobenen Gewerbebetriebe im", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("Staaten und Landestheile.", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("Noch:", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("", protectedHeader, true));
        transformations.add(new DeleteRowByType("labelOrd"));
        transformations.add(new DeleteMatchingRow("*) Darunter ", protectedHeader, false));

        transformations.add(new ExtractColumns(0, 11));
        transformations.add(new DeleteColumn(1));

        transformations.add(new CombineLabelRows(protectedHeader));


        transformations.add(new TrimValues());
        transformations.add(new SanityNotEmpty());

        transformations.add(new ResolveWrongLabels(2));

        transformations.add(new DeleteMatchingRow("Wiederholung. G", protectedHeader, false));
        transformations.add(new DeleteMatchingRow("*) Bei", protectedHeader, false));

        transformations.add(new NormalizeCompoundTables("label", 2));
        transformations.add(new CleanFacts());

        String[] headerLabels = new String[7];
        headerLabels[0] = "Anzahl";
        headerLabels[1] = "Beschreibung";
        headerLabels[2] = "Betriebe";
        headerLabels[3] = "Kategorie - Betrieb";
        headerLabels[4] = "Kategorie - Ort";
        headerLabels[5] = "Ort";
        headerLabels[6] = "Fakten Details";
        transformations.add(new CreateHeaders(headerLabels));

        String label = "Übersicht Gewerbebetriebe";
        String description = "Übersicht Gewerbebetriebe 1. Dezember 1875";
        String importer = "https://github.com/bayerls";
        transformations.add(new AddMetadata(label, description, importer));




        return transformations;
    }
}
