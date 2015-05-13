package de.bayerl.statistics.gui.controller;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.rdf.model.Model;
import de.bayerl.statistics.gui.model.TransformationModel;
import de.bayerl.statistics.instance.Config;
import de.bayerl.statistics.instance.Conversion;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.TableSliceType;
import de.bayerl.statistics.transformer.Transformation;
import org.apache.jena.riot.Lang;

import java.io.*;
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
        List<Table> tables = Loader.loadFiles(files.get(0));

        // Merge tables into the first table
        Table table = tables.remove(0);
        for (Table t : tables) {
            table.getRows().addAll(t.getRows());
            table.getMetadata().getSources().add(t.getMetadata().getSources().get(0));
        }

        System.out.println(tables.size() + 1 + " Table(s) loaded (and merged) in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
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

    public static List<String> transform(List<File> files, List<TransformationModel> transformations, String htmlFolder) {
        Stopwatch singleStepWatch = Stopwatch.createStarted();
        List<String> correspondingNames = new ArrayList<>();
        Table tTable = load(files, htmlFolder);
        int i = 0;
        Class c;
        List<Object> parameterList = new ArrayList<>();
        Object[] parameters;
        for(TransformationModel m : transformations) {
            StringBuilder builder = new StringBuilder();
            ++i;
            try {
                c = Class.forName("de.bayerl.statistics.transformer." + m.getName());
                for (int j = 0; j < m.getAttributes().size(); j++) {
                    if (m.getAttributes().get(j).hasStringList()) {
                        if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("Cell")) {
                            Cell cell = new Cell();
                            cell.setRole(m.getAttributes().get(j).getStringList().get(0));
                            cell.getValue().setValue(m.getAttributes().get(j).getStringList().get(1));
                            builder.append("_" + "{" + m.getAttributes().get(j).getStringList().get(0)
                                    + "," + m.getAttributes().get(j).getStringList().get(1) + "}");
                            parameterList.add(cell);
                        } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String[]")) {
                            String[] temp = new String[m.getAttributes().get(j).getStringList().size()];
                            builder.append("_" + "{");
                            for (int g = 0; g < m.getAttributes().get(j).getStringList().size(); g++) {
                                temp[g] = m.getAttributes().get(j).getStringList().get(g);
                                builder.append(m.getAttributes().get(j).getStringList().get(g));
                                if(g != m.getAttributes().get(j).getStringList().size()-1) {
                                    builder.append(",");
                                }
                            }
                            builder.append("}");
                            parameterList.add(temp);
                        }

                    } else if (m.getAttributes().get(j).hasIntList() && c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int[]")) {
                        int[] temp = new int[m.getAttributes().get(j).getIntList().size()];
                        builder.append("_" + "{");
                        for (int g = 0; g < m.getAttributes().get(j).getIntList().size(); g++) {
                            temp[g] = m.getAttributes().get(j).getIntList().get(g);
                            builder.append(m.getAttributes().get(j).getIntList().get(g));
                            if(g != m.getAttributes().get(j).getIntList().size()-1) {
                                builder.append(",");
                            }
                        }
                        builder.append("}");
                        parameterList.add(temp);
                    } else if (m.getAttributes().get(j).hasIntValue() && c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int")) {
                        parameterList.add(m.getAttributes().get(j).getIntValue());
                        builder.append("_" + m.getAttributes().get(j).getIntValue());
                    } else if(c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String")) {
                        parameterList.add(m.getAttributes().get(j).getValue());
                        builder.append("_" + m.getAttributes().get(j).getValue());
                    } else if(c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("TableSliceType")){
                        if(m.getAttributes().get(j).getValue().equals("Row")) {
                            parameterList.add(TableSliceType.ROW);
                            builder.append("_" + "Row");
                        } else {
                            parameterList.add(TableSliceType.COLUMN);
                            builder.append("_" + "Col");
                        }
                    }
                }
                parameters = new Object[parameterList.size()];
                for (int j = 0; j < parameterList.size(); j++) {
                    parameters[j] = parameterList.get(j);
                }
                parameterList.clear();
                Transformation t;
                if(parameters.length > 0) {
                    t = (Transformation) c.getConstructors()[0].newInstance(parameters);
                } else {
                    t = (Transformation) c.getConstructors()[0].newInstance();
                }
                tTable = t.transform(tTable);
                System.out.println("Table processed in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms. " + c.getSimpleName());
                singleStepWatch.reset();
                singleStepWatch.start();
                Printer.printHTML(tTable, i + "_" + c.getSimpleName() + builder.toString(), htmlFolder);
                correspondingNames.add("table_" + i);
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

        private static Table deepCopy(Table table) {
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(table);
                oos.close();

                final ObjectInputStream ois = new ObjectInputStream(
                        new ByteArrayInputStream(baos.toByteArray()));
                final Table clone = (Table) ois.readObject();

                return clone;
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Cloning failed");
            }
        }


}
