package de.bayerl.statistics;

import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.HashMap;

@Deprecated
public class TeiParser {

    private final static String FOLDER = "/Users/sebastianbayerl/Desktop/digitaleReichsstatistik/data/bd020/";
    private final static String FOLDER_TARGET = "/Users/sebastianbayerl/Desktop/digitaleReichsstatistik/data/converted/";

    public static void transformFiles() {

        File folder = new File(FOLDER);

        for (File file : folder.listFiles()) {
            if (!file.getName().equals(".DS_Store")) {
                String content = loadWithTagSoup(file.getName());
                writeContent(file.getName(), content);
            }
        }
    }


    private static void writeContent(String fileName, String content) {
        fileName += ".html";

        File file = new File(FOLDER_TARGET + fileName);
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



    private static String loadWithTagSoup(String fileName) {
        final StringBuilder sb = new StringBuilder();
        final Boolean[] inCell = {false};
        sb.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta charset=\"utf-8\"/>\n" +
                "    </head>\n" +
                "    \n" +
                "    <body>");
        sb.append("<table border=\"1\">");

        SAXParserImpl parser = null;

        try {
            parser = SAXParserImpl.newInstance(new HashMap());
        } catch (SAXException e) {
            e.printStackTrace();
        }

        if (parser != null) {
            File file = new File(FOLDER + fileName);
            try {
                parser.parse(file, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName,  String name, Attributes a) {
                       // System.out.println(name);

                        if (name.equalsIgnoreCase("row")) {
                            sb.append("<tr>");
                        } else if (name.equalsIgnoreCase("cell")) {
                            inCell[0] = true;
                            sb.append("<td ");
                            String value = a.getValue("cols");

                            if (value != null) {
                                sb.append("colspan=\"" + value + "\" ");
                            }

                            value = a.getValue("rows");

                            if (value != null) {
                                sb.append("rowspan=\"" + value + "\" ");
                            }

                            value = a.getValue("role");

                            if (value != null && value.equals("data")) {
                                sb.append("style=\"background-color: #E6FEE6\"");
                            } else if (value != null && value.equals("label")) {
                                sb.append("style=\"background-color: #6495ED\"");
                            } else if (value != null && value.equals("dataRes")) {
                                sb.append("style=\"background-color: #A9A9A9\"");
                            } else if (value != null && value.equals("labelUnit")) {
                                sb.append("style=\"background-color: #808000\"");
                            } else  if (value != null && value.equals("labelOrd")) {
                                sb.append("style=\"background-color: #9370DB\"");
                            } else  if (value != null && value.equals("dataMeasure")) {
                                    sb.append("style=\"background-color: #FFFF00\"");
                            } else  if (value != null && value.equals("date")) {
                                sb.append("style=\"background-color: #A52A2A\"");
                            } else  if (value != null && value.equals("labelErg")) {
                                sb.append("style=\"background-color: #40E0D0\"");

                            } else if (value != null) {
                                System.out.println(value);
                            }



                            sb.append(">");
                        } else if (name.equalsIgnoreCase("lb")) {
                            sb.append(" ");
                        }
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) {
                        //System.out.println("- " + qName);

                        if (qName.equalsIgnoreCase("row")) {
                            sb.append("</tr>");
                            inCell[0] = false;

                        } else if (qName.equalsIgnoreCase("cell")) {
                            sb.append("</td>");
                        }
                    }

                    @Override
                    public void characters(char ch[], int start, int length) {

                        if (length != 1) {
                            if (inCell[0]) {
                                sb.append(new String(ch, start, length));
                            }
                        }
                    }
                });
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sb.append("        </table>\n" +
                "    </body>\n" +
                "</html>");

        return sb.toString();
    }

}
