package de.bayerl.statistics.gui.controller;

import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.model.Cell;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Generates a HTML representation of the table or prints it to standard out.
 */
public class Printer {

    public static void print(Table table) {
        for (Row row : table.getRows()) {
            for (Cell cell : row.getCells()) {
                System.out.print(cell.getRows() + "/" + cell.getCols() + " -- ");
            }
            System.out.println();
        }
    }

    public static void printHTML(Table table, String filenamePart, String htmlLocation) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n" +
                "<html><head><meta charset=\"utf-8\"/><style type=\"text/css\">td {border: 1px solid black;}</style></head><body>\n");
        sb.append("<table>");

        for (Row row : table.getRows()) {
            sb.append("<tr>");

            for (Cell cell : row.getCells()) {
                sb.append("<td ");
                sb.append("colspan=\"" + cell.getCols() + "\" ");
                sb.append("rowspan=\"" + cell.getRows() + "\" ");
                String value = cell.getRole();

                if (value.equals("data")) {
                    sb.append("style=\"background-color: #E6F5CC\"");
                } else if (value.equals("label")) {
                    sb.append("style=\"background-color: #6495ED\"");
                } else if (value.equals("dataRes")) {
                    sb.append("style=\"background-color: #A9A9A9\"");
                } else if (value.equals("labelUnit")) {
                    sb.append("style=\"background-color: #808000\"");
                } else if (value.equals("labelOrd")) {
                    sb.append("style=\"background-color: #9370DB\"");
                } else if (value.equals("dataMeasure")) {
                    sb.append("style=\"background-color: #FFFF00\"");
                } else if (value.equals("date")) {
                    sb.append("style=\"background-color: #A52A2A\"");
                } else if (value.equals("labelErg")) {
                    sb.append("style=\"background-color: #40E0D0\"");
                }

                sb.append(">");
                sb.append(cell.getValue().getValue());
                sb.append("</td>");
            }

            sb.append("</tr>");
        }

        sb.append("</table></body></html>");

        writeContent("table_" + filenamePart, sb.toString(), htmlLocation);
    }


    private static void writeContent(String fileName, String content, String htmlLocation) {
        File folder = new File(htmlLocation);

        if (!folder.exists()) {
            folder.mkdir();
        }

        fileName += ".html";
        File file = new File(folder.getAbsolutePath() + File.separator + fileName);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        writer.write(content);
        writer.close();
    }

}
