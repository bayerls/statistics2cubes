package de.bayerl.statistics;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.rdf.model.Model;
import de.bayerl.statistics.converter.Table2CubeConverter;
import de.bayerl.statistics.instance.Config;
import de.bayerl.statistics.instance.Conversion;
import de.bayerl.statistics.instance.Example1;
import de.bayerl.statistics.instance.Example2;
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

    // TODO example 2 020_0032_018.?

    public static void handle() {


        Conversion conversion = new Example2();


        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch singleStepWatch = Stopwatch.createStarted();

        // Load table
        List<Table> tables = TeiLoader.loadFiles(conversion);

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

        TablePrinter.printHTML(table, "0_original", conversion);

        int i = 0;
        // do transformations
        for (Transformation transformer : conversion.getTransformations()) {
            table = transformer.transform(table);
            i++;
            System.out.println("Table processed in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms. " + transformer.getName());
            singleStepWatch.reset();
            singleStepWatch.start();
            TablePrinter.printHTML(table, "" + i + "_" + transformer.getName(), conversion);
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

        write2File(model, conversion);
        System.out.println("Cube persisted " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.reset();
        singleStepWatch.start();

        System.out.println("Done in (total): " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
    }

    private static void write2File(Model model, Conversion conversion) {
        File folder = new File(Config.FOLDER + conversion.getFolder() + Config.FOLDER_N3);

        if (!folder.exists()) {
            folder.mkdir();
        }

        File output = new File(Config.FOLDER + conversion.getFolder() + Config.FOLDER_N3 + "dump.n3");
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
