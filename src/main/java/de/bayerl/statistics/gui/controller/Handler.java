package de.bayerl.statistics.gui.controller;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.rdf.model.Model;
import de.bayerl.statistics.gui.model.TransformationModel;
import de.bayerl.statistics.instance.Config;
import de.bayerl.statistics.instance.Conversion;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.transformer.Transformation;
import org.apache.jena.riot.Lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Handler {

    private static final String SPLITTER = "~#~LB~#~";

    public static Table load(List<File> files, String htmlFolder) {
        Stopwatch singleStepWatch = Stopwatch.createStarted();

        // Load table
        List<Table> tables = Loader.loadFiles(files);

        // Merge tables into the first table
        Table table = tables.remove(0);
        for (Table t : tables) {
            table.getRows().addAll(t.getRows());
            table.getMetadata().getSources().add(t.getMetadata().getSources().get(0));
        }

        System.out.println(tables.size() + " Table(s) loaded (and merged) in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.reset();
        singleStepWatch.start();

        // delete old de.bayerl.statistics.gui.html files before printing new ones
        File dir = new File(htmlFolder);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for(File file : dir.listFiles()) {
            file.delete();
        }

        Printer.printHTML(table, "0_original", htmlFolder);

        return table;
    }

    public static List<String> transform(Table table, List<TransformationModel> transformations, String htmlFolder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch singleStepWatch = Stopwatch.createStarted();
        List<String> correspondingNames = new ArrayList<>();
        String returnString = "";
        int i = 0;
        Class c;
        Class d;
        Method transform = null;
        try {
            d = Class.forName("de.bayerl.statistics.transformer.Transformation");
            transform = d.getDeclaredMethod("transform", new Class[]{Table.class});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        }
        List<Object> parameterList = new ArrayList<>();
        Object[] parameters;
        for(TransformationModel m : transformations) {
            ++i;
            try {
                c = Class.forName("de.bayerl.statistics.transformer." + m.getName());

                for (int j = 0; j < m.getAttributes().size(); j++) {
                    if (m.getAttributes().get(j).hasStringList()) {
                        if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("Cell")) {
                            Cell cell = new Cell();
                            cell.setRole(m.getAttributes().get(j).getStringList().get(0));
                            cell.getValue().setValue(m.getAttributes().get(j).getStringList().get(1));
                            parameterList.add(cell);
                        } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String[]")) {
                            String[] temp = new String[m.getAttributes().get(j).getStringList().size()];
                            for (int g = 0; g < m.getAttributes().get(j).getStringList().size(); g++) {
                                temp[g] = m.getAttributes().get(j).getStringList().get(g);
                            }
                            parameterList.add(temp);
                        }

                    } else if (m.getAttributes().get(j).hasIntList() && c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int[]")) {
                        int[] temp = new int[m.getAttributes().get(j).getIntList().size()];
                        for (int g = 0; g < m.getAttributes().get(j).getIntList().size(); g++) {
                            temp[g] = m.getAttributes().get(j).getIntList().get(g);
                        }
                        parameterList.add(temp);
                    } else if (m.getAttributes().get(j).hasIntValue() && c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int")) {
                        parameterList.add(m.getAttributes().get(j).getIntValue());
                    } else {
                        if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String")) {
                            parameterList.add(m.getAttributes().get(j).getValue());
                        }
                    }
                }
                parameters = new Object[parameterList.size()];
                for (int j = 0; j < parameterList.size(); j++) {
                    parameters[j] = parameterList.get(j);
                }
                parameterList.clear();
                Transformation t = (Transformation) c.getConstructors()[0].newInstance(parameters);
                transform.invoke(t, table);
                System.out.println("Table processed in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms. " + c.getSimpleName());
                singleStepWatch.reset();
                singleStepWatch.start();
                Printer.printHTML(table, i + "_" + c.getSimpleName(), htmlFolder);
                correspondingNames.add(i + "_" + c.getSimpleName() + ".html");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();

            } catch (InstantiationException ie) {
                ie.printStackTrace();

            } catch (IllegalAccessException iae) {
                iae.printStackTrace();

            } catch (InvocationTargetException ite) {
                ite.printStackTrace();

            }
        }
            return correspondingNames;

        }

//        if (table.getHeaders().size() == 0) {
//            System.out.println("Headers are not set. Necessary for triplification.");
//        } else {
//            // remove line numbers
//            DeleteRowColNumbers deleteRowColNumbers = new DeleteRowColNumbers();
//            table = deleteRowColNumbers.transform(table);
//
//            // convert to rdf
//            Table2CubeConverter table2CubeConverter = new Table2CubeConverter(table);
//            Model model = table2CubeConverter.convert();
//
//            System.out.println("Table converted in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
//            singleStepWatch.reset();
//            singleStepWatch.start();
//
//            // write to file
//            write2File(model, Config.CONVERSION);
//            System.out.println("Cube persisted " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
//            System.out.println("Done in (total): " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
//        }

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