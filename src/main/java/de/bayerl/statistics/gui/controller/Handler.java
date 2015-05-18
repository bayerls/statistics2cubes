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

public class Handler {

    public static Table load(List<File> files, String htmlFolder) {
        Stopwatch singleStepWatch = Stopwatch.createStarted();

        // Load table
        List<Table> tables = Loader.loadFiles(files.get(0));

        Table table = mergeTables(tables);
        System.out.println(tables.size() + 1 + " Table(s) loaded (and merged) in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        singleStepWatch.stop();

        deleteHtmls(htmlFolder);
        Printer.printHTML(table, "0_original", htmlFolder);

        return table;
    }

    private static Table mergeTables(List<Table> tables) {
        Table table = tables.remove(0);
        for (Table t : tables) {
            table.getRows().addAll(t.getRows());
            table.getMetadata().getSources().add(t.getMetadata().getSources().get(0));
        }
        table = (new AddRowColNumbers()).transform(table);
        return table;
    }

    private static void deleteHtmls(String htmlFolder) {
        File dir = new File(htmlFolder);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }

    private static String createFileName(TransformationModel m, Class c) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < m.getAttributes().size(); j++) {
            if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("Cell")) {
                builder.append("_{");
                builder.append(m.getAttributes().get(j).getStringList().get(0));
                builder.append(",");
                builder.append(m.getAttributes().get(j).getStringList().get(1));
                builder.append("}");
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String[]")) {
                builder.append("_{");
                for (int g = 0; g < m.getAttributes().get(j).getStringList().size(); g++) {
                    builder.append(m.getAttributes().get(j).getStringList().get(g));
                    if (g != m.getAttributes().get(j).getStringList().size() - 1) {
                        builder.append(",");
                    }
                }
                builder.append("}");

            } else if (m.getAttributes().get(j).hasIntList() && c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int[]")) {
                builder.append("_{");
                for (int g = 0; g < m.getAttributes().get(j).getIntList().size(); g++) {
                    builder.append(m.getAttributes().get(j).getIntList().get(g));
                    if (g != m.getAttributes().get(j).getIntList().size() - 1) {
                        builder.append(",");
                    }
                }
                builder.append("}");
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("TableSliceType")) {
                builder.append("_");
                builder.append(m.getAttributes().get(j).getValue());

            } else {
                builder.append("_");
                builder.append(m.getAttributes().get(j).getValue());
            }
        }
        return  builder.toString();
    }

    private static Cell createCell(Parameter p) {
        Cell cell = new Cell();
        cell.setRole(p.getStringList().get(0));
        cell.getValue().setValue(p.getStringList().get(1));
        return cell;
    }

    private static String[] createStringArray(Parameter p) {
        String[] temp = new String[p.getStringList().size()];
        for (int g = 0; g < p.getStringList().size(); g++) {
            temp[g] = p.getStringList().get(g);
        }
        return temp;
    }

    private static int[] createIntArray(Parameter p) {
        int[] temp = new int[p.getIntList().size()];
        for (int g = 0; g < p.getIntList().size(); g++) {
            temp[g] = p.getIntList().get(g);
        }
        return temp;
    }

    private static List<Object> fillParameterList(TransformationModel m, Class c) {
        List<Object> list = new ArrayList<>();
        for (int j = 0; j < m.getAttributes().size(); j++) {
            if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("Cell")) {
                Cell cell = createCell(m.getAttributes().get(j));
                list.add(cell);
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String[]")) {
                String[] temp = createStringArray(m.getAttributes().get(j));
                list.add(temp);
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int[]")) {
                int[] temp = createIntArray(m.getAttributes().get(j));
                list.add(temp);
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("int")) {
                list.add(Integer.parseInt(m.getAttributes().get(j).getValue()));
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName().equals("String")) {
                list.add(m.getAttributes().get(j).getValue());
            } else if (c.getConstructors()[0].getParameterTypes()[j].getSimpleName()
                    .equals("TableSliceType") && m.getAttributes().get(j).getValue().equals("Row")) {
                list.add(TableSliceType.ROW);
            } else {
                list.add(TableSliceType.COLUMN);
            }
        }
        return list;
    }

    public static List<Object> transform(List<File> files, List<TransformationModel> transformations, String htmlFolder) {
        Stopwatch stopWatch = Stopwatch.createStarted();
        Stopwatch singleStepWatch = Stopwatch.createStarted();
        List<String> correspondingNames = new ArrayList<>();
        Table tTable = load(files, htmlFolder);
        int i = 0;
        for (TransformationModel m : transformations) {
            ++i;
            try {
                Class c = Class.forName("de.bayerl.statistics.transformer." + m.getName());
                String fileName = createFileName(m, c);
                List<Object> parameterList = fillParameterList(m, c);
                Object[] parameters = new Object[parameterList.size()];
                for (int j = 0; j < parameterList.size(); j++) {
                    parameters[j] = parameterList.get(j);
                }
                if(!m.getName().contains("RowColNumbers")) {
                    Transformation t;
                    if (parameters.length > 0) {
                        t = (Transformation) c.getConstructors()[0].newInstance(parameters);
                    } else {
                        t = (Transformation) c.getConstructors()[0].newInstance();
                    }
                    tTable = t.transform(tTable);
                } else {
                    MetaTransformation t;
                    if (parameters.length > 0) {
                        t = (MetaTransformation) c.getConstructors()[0].newInstance(parameters);
                    } else {
                        t = (MetaTransformation) c.getConstructors()[0].newInstance();
                    }
                    tTable = t.transform(tTable);
                }
                System.out.println("Table processed in " + singleStepWatch.elapsed(TimeUnit.MILLISECONDS) + " ms. " + c.getSimpleName() + fileName);
                singleStepWatch.reset();
                singleStepWatch.start();
                Printer.printHTML(tTable, i + "_" + c.getSimpleName() + fileName, htmlFolder);
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

    private static String convertModelToString(Model model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, Lang.N3.getName());

        return baos.toString();
    }

}