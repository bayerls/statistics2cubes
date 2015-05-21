package de.bayerl.statistics.gui.controller;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.rdf.model.Model;
import de.bayerl.statistics.gui.model.Parameter;
import de.bayerl.statistics.gui.model.TransformationModel;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.TableSliceType;
import de.bayerl.statistics.transformer.AddRowColNumbers;
import de.bayerl.statistics.transformer.DeleteRowColNumbers;
import de.bayerl.statistics.transformer.MetaTransformation;
import de.bayerl.statistics.transformer.Transformation;
import org.apache.jena.riot.Lang;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class that handles loading, transformation and export of tables
 */
public class Handler {

    /**
     * Loads several tei-tables, merges it into one single table and creates an html-representation of the merged table
     * on the file system.
     *
     * @param files tables to be merged
     * @param htmlFolder location for the created html-tables
     * @return merged table
     */
    public static Table load(List<File> files, String htmlFolder) {
        Stopwatch singleStepWatch = Stopwatch.createStarted();

        // Load tables
        List<Table> tables = Loader.loadFiles(files.get(0));

        Table table = mergeTables(tables);
        System.out.println(tables.size() + 1 + " Table(s) loaded (and merged) in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.stop();

        deleteHtmls(htmlFolder);
        Printer.printHTML(table, "0_original", htmlFolder);

        return table;
    }

    /**
     * Merges the given List of tables into one single table.
     *
     * @param tables list of tables to be merged
     * @return merged table
     */
    private static Table mergeTables(List<Table> tables) {
        Table table = tables.remove(0);
        for (Table t : tables) {
            table.getRows().addAll(t.getRows());
            table.getMetadata().getSources().add(t.getMetadata().getSources().get(0));
        }
        table = (new AddRowColNumbers()).transform(table);
        return table;
    }

    /**
     * Deletes all html-files in the given filepath.
     *
     * @param htmlFolder filepath for htmls to delete
     */
    private static void deleteHtmls(String htmlFolder) {
        File dir = new File(htmlFolder);
        if (!dir.exists()) {
            dir.mkdir();
        }

        for (File file : dir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Converts the given Parameter into a cell needed for the transformation-constructor.
     *
     * @param p Parameter to convert
     * @return cell needed for transformation creation
     */
    private static Cell createCell(Parameter p) {
        Cell cell = new Cell();
        cell.setRole(p.getStringList().get(0));
        cell.getValue().setValue(p.getStringList().get(1));
        return cell;
    }

    /**
     * Converts the given Parameter into a StringArray needed for the transformation-constructor.
     *
     * @param p Parameter to convert
     * @return StringArray needed for transformation creation
     */
    private static String[] createStringArray(Parameter p) {
        String[] temp = new String[p.getStringList().size()];
        for (int g = 0; g < p.getStringList().size(); g++) {
            temp[g] = p.getStringList().get(g);
        }
        return temp;
    }

    /**
     * Converts the given Parameter into an IntArray needed for the transformation-constructor.
     *
     * @param p Parameter to convert
     * @return IntArray needed for transformation creation
     */
    private static int[] createIntArray(Parameter p) {
        int[] temp = new int[p.getIntList().size()];
        for (int g = 0; g < p.getIntList().size(); g++) {
            temp[g] = p.getIntList().get(g);
        }
        return temp;
    }

    /**
     * Creates the list of parameters, that is needed for transformation-construction.
     *
     * @param m TransformationModel to get the parameters from
     * @param c Class to get the constructor-argument types from
     * @return List of parameters needed when calling the constructor
     */
    private static List<Object> fillParameterList(TransformationModel m, Class c) {
        List<Object> list = new ArrayList<>();
        for (int j = 0; j < m.getAttributes().size(); j++) {

            // Constructor needs a cell as parameter
            if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("Cell")) {
                Cell cell = createCell(m.getAttributes().get(j));
                list.add(cell);

            // Constructor needs a StringArray as parameter
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String[]")) {
                String[] temp = createStringArray(m.getAttributes().get(j));
                list.add(temp);

            // Constructor needs an IntArray as parameter
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int[]")) {
                int[] temp = createIntArray(m.getAttributes().get(j));
                list.add(temp);

            // Constructor needs an Integer as parameter
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int")) {
                list.add(Integer.parseInt(m.getAttributes().get(j).getValue()));

            // Constructor needs a String as parameter
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String")) {
                list.add(m.getAttributes().get(j).getValue());

            // Constructor needs TableSliceType as parameter
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName()
                    .equals("TableSliceType") && m.getAttributes().get(j).getValue().equals("Row")) {
                list.add(TableSliceType.ROW);
            } else {
                list.add(TableSliceType.COLUMN);
            }
        }
        return list;
    }

    /**
     * Executes the given list of Transformations on the given tables
     *
     * @param files tables to transform
     * @param transformations list of transformations that shall be executed
     * @param htmlFolder filepath to save html-files
     * @return List that contains the table after the last transformation and a list of names of the created html-files
     */
    public static List<Object> transform(List<File> files, List<TransformationModel> transformations, String htmlFolder) {
        Stopwatch stopWatch = Stopwatch.createStarted();
        Stopwatch singleStepWatch = Stopwatch.createStarted();
        List<String> correspondingNames = new ArrayList<>();

        // load tables to transform
        Table tTable = load(files, htmlFolder);
        int i = 0;
        for (TransformationModel m : transformations) {
            ++i;
            try {

                // Get class for given transformationname
                Class c = Class.forName("de.bayerl.statistics.transformer." + m.getName());
                List<Object> parameterList = fillParameterList(m, c);

                // convert parameterlist into array
                Object[] parameters = new Object[parameterList.size()];
                for (int j = 0; j < parameterList.size(); j++) {
                    parameters[j] = parameterList.get(j);
                }

                // Transformation?
                if(!m.getName().contains("RowColNumbers")) {
                    Transformation t = (Transformation) c.getConstructors()[0].newInstance(parameters);
                    tTable = t.transform(tTable);

                // MetaTransformation?
                } else {
                    MetaTransformation t = (MetaTransformation) c.getConstructors()[0].newInstance(parameters);
                    tTable = t.transform(tTable);
                }

                System.out.println("Table processed in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms. " + c.getSimpleName());
                singleStepWatch.reset();
                singleStepWatch.start();
                Printer.printHTML(tTable, i + "_" + c.getSimpleName(), htmlFolder);
                correspondingNames.add("table_" + i);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Transformations executed in " + stopWatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
        stopWatch.stop();
        List<Object> returnList = new ArrayList<>();
        returnList.add(tTable);
        returnList.add(correspondingNames);
        return returnList;

    }

    /**
     * Exports the given table in cube format
     *
     * @param table table to export
     * @param version export version
     * @param folder folder for n3-files
     */
    public static void export(Table table, String version, String folder) {
        Stopwatch singleStepWatch = Stopwatch.createStarted();
        if (table.getHeaders().size() == 0) {
            System.out.println("Headers are not set. Necessary for triplification.");
        } else {
            // remove line numbers
            DeleteRowColNumbers deleteRowColNumbers = new DeleteRowColNumbers();
            table = deleteRowColNumbers.transform(table);

            // convert to rdf
            Converter table2CubeConverter = new Converter(table, version);
            Model model = table2CubeConverter.convert();

            System.out.println("Table converted in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
            singleStepWatch.reset();
            singleStepWatch.start();

            // write to file
            write2File(model, folder, version);
            System.out.println("Cube persisted " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        }
    }

    /**
     * Writes table to file in n3-format
     *
     * @param model model to convert
     * @param n3Folder n3- filepath
     * @param version n3-version
     */
    private static void write2File(Model model, String n3Folder, String version) {
        File folder = new File(n3Folder);

        if (!folder.exists()) {
            folder.mkdir();
        }
        String filename = "dump";
        if (version.equals("1.2")) {
            filename += "_1.2";
        } else {
            filename += "_1.1";
        }

        filename += ".n3";
        File output = new File(n3Folder + File.separator + filename);
        System.out.println(output.getAbsolutePath());

        try (FileWriter fw = new FileWriter(output)) {
            fw.write(convertModelToString(model));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Converts the given model to a String
     *
     * @param model
     * @return String-representation of given model
     */
    private static String convertModelToString(Model model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, Lang.N3.getName());

        return baos.toString();
    }

}