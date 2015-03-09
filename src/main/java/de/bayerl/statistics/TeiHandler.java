package de.bayerl.statistics;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.rdf.model.Model;
import de.bayerl.statistics.converter.Table2CubeConverter;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.transformer.*;
import org.apache.jena.riot.Lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TeiHandler {

    private static final String FOLDER_TARGET = "";

    // TODO trim values
    // TODO move table specific code like transformations and paths to a single class?

    // TODO example 1 complete?
    // TODO example 2 020_0032_018.?

    public static void handle() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch singleStepWatch = Stopwatch.createStarted();

        // Load table
        List<Table> tables = TeiLoader.loadFiles();

        // only work with a small subset
//        tables = tables.subList(0, 10);

        // Merge tables into the first table
        Table table = tables.remove(0);
        for (Table t : tables) {
            table.getRows().addAll(t.getRows());
            table.getMetadata().getSources().add(t.getMetadata().getSources().get(0));
        }

        System.out.println(tables.size() + " Table(s) loaded (and merged) in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.reset();
        singleStepWatch.start();

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

        transformations.add(new NormalizeCompoundTables());

        // after normalization
//        transformations.add(new SplitColumn("─", 4));
        transformations.add(new CreateHeaders());

        TablePrinter.printHTML(table, "0_original");

        int i = 0;
        // do transformations
        for (Transformation transformer : transformations) {
            table = transformer.transform(table);
            i++;
            System.out.println("Table processed in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms. " + transformer.getName());
            singleStepWatch.reset();
            singleStepWatch.start();
            TablePrinter.printHTML(table, "" + i + "_" + transformer.getName());
        }

        // remove line numbers
        DeleteRowColNumbers deleteRowColNumbers = new DeleteRowColNumbers();
        table = deleteRowColNumbers.transform(table);

        // TODO this can be a transformation?
        Table2CubeConverter table2CubeConverter = new Table2CubeConverter(table);
        Model model = table2CubeConverter.convert();

        System.out.println("Table converted in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.reset();
        singleStepWatch.start();

        write2File(model);
        System.out.println("Cube persisted " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.reset();
        singleStepWatch.start();

        System.out.println("Done in (total): " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
    }

    private static void write2File(Model model) {
        // TODO filname + target?
        File output = new File(FOLDER_TARGET + "dump.n3");
        try {
            FileWriter fw = new FileWriter(output);
            fw.write(convertModelToString(model));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String convertModelToString(Model model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, Lang.N3.getName());

        return baos.toString();
    }

}
