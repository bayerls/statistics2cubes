package de.bayerl.statistics;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.rdf.model.Model;
import de.bayerl.statistics.converter.Table2CubeConverter;
import de.bayerl.statistics.instance.Config;
import de.bayerl.statistics.instance.Conversion;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.persitance.BigdataEndpoint;
import de.bayerl.statistics.persitance.ContentTypeRdf;
import de.bayerl.statistics.persitance.VirtuosoDao;
import de.bayerl.statistics.transformer.DeleteRowColNumbers;
import de.bayerl.statistics.transformer.Transformation;
import org.apache.jena.riot.Lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TeiHandler {

    public static void handle() {
        Conversion conversion = Config.CONVERSION;
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch singleStepWatch = Stopwatch.createStarted();

        // Load table
        List<Table> tables = TeiLoader.loadFiles(conversion);

        // only work with a small subset
        if (Config.LIMIT > 0) {
            tables = tables.subList(0, Config.LIMIT);
        }

        // Merge tables into the first table
        Table table = tables.remove(0);
        for (Table t : tables) {

            table.getRows().addAll(t.getRows());
            table.getMetadata().getSources().add(t.getMetadata().getSources().get(0));
        }

        System.out.println(tables.size() + " Table(s) loaded (and merged) in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.reset();
        singleStepWatch.start();

        // delete old html files before printing new ones
        File dir = new File(Config.FOLDER + conversion.getFolder() + Config.FOLDER_HTML);
        for(File file : dir.listFiles()) {
            file.delete();
        }

        TablePrinter.printHTML(table, "0_original", conversion);

        int i = 0;
        // do transformations
        for (Transformation transformer : conversion.getTransformations()) {
            i++;
            table = transformer.transform(table);
            System.out.println("Table processed in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms. " + transformer.getName());
            singleStepWatch.reset();
            singleStepWatch.start();
            TablePrinter.printHTML(table, "" + i + "_" + transformer.getName(), conversion);
        }

        if (table.getHeaders().size() == 0) {
            System.out.println("Headers are not set. Necessary for triplification.");
        } else {
            // remove line numbers
            DeleteRowColNumbers deleteRowColNumbers = new DeleteRowColNumbers();
            table = deleteRowColNumbers.transform(table);


            List<Row> rows = new ArrayList<>();

            for (int j = 0; j < 250; j++) {
                rows.add(table.getRows().get(j));
            }

            table.setRows(rows);



            // convert to rdf
            Table2CubeConverter table2CubeConverter = new Table2CubeConverter(table);
            //Model model = table2CubeConverter.convert();

//
            Model structure =  table2CubeConverter.createStructure();
            Model obs = table2CubeConverter.createObservations();
            obs.add(structure);

            System.out.println("Table converted in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
            singleStepWatch.reset();
            singleStepWatch.start();


            String uuid = UUID.randomUUID().toString();
            String context = "http://42-data.org/resource/cube/demo-small-" + uuid;
            BigdataEndpoint bigdataEndpoint = new BigdataEndpoint();
            bigdataEndpoint.persist(obs, ContentTypeRdf.N3, context);

//            VirtuosoDao dao = new VirtuosoDao();
//            dao.importUpload(structure, context);
//            dao.importUpload(obs  , "http://42-data.org/resource/cube/demo");

            // write to file


            write2File(obs, conversion);
            System.out.println("Cube persisted " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
            System.out.println("Done in (total): " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        }
    }

    private static void write2File(Model model, Conversion conversion) {
        File folder = new File(Config.FOLDER + conversion.getFolder() + Config.FOLDER_N3);

        if (!folder.exists()) {
            folder.mkdir();
        }
        String filename = "dump";
        if (Config.GENERATE_1_2) {
            filename += "_1.2";
        } else {
            filename += "_1.1";
        }

        filename += ".n3";
        File output = new File(Config.FOLDER + conversion.getFolder() + Config.FOLDER_N3 + filename);

        try (FileWriter fw = new FileWriter(output)) {
            fw.write(convertModelToString(model));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static String convertModelToString(Model model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, Lang.N3.getName());

        return baos.toString();
    }

}
