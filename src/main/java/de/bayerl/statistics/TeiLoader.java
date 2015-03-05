package de.bayerl.statistics;

import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;
import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TeiLoader {

    private final static String FOLDER = "sampleData/1/";
    private final static String FILE = "drsa_020_0040_026.tei";

    public final static String PLACEHOLDER_LB = "##lb##";

    public static List<Table> loadFiles() {
        List<String> linkGroup = loadLinkGroup();
        List<Table> tables = linkGroup.stream().map(TeiLoader::load).collect(Collectors.toList());

        return tables;
    }

    private static List<String> loadLinkGroup() {
        SAXParserImpl parser = null;
        List<String> links = new ArrayList<>();

        try {
            parser = SAXParserImpl.newInstance(new HashMap());
        } catch (SAXException e) {
            e.printStackTrace();
        }

        if (parser != null) {
            File file = new File(FOLDER + FILE);

            try {
                parser.parse(file, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName,  String name, Attributes a) {
                        if (name.equalsIgnoreCase("link")) {
                            String link = a.getValue("target");
                            links.add(link);
                        }
                    }
                });
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return links;
    }


    private static Table load(String filename) {
        SAXParserImpl parser = null;
        Table table = new Table();

        try {
            parser = SAXParserImpl.newInstance(new HashMap());
        } catch (SAXException e) {
            e.printStackTrace();
        }

        if (parser != null) {
            File file = new File(FOLDER + filename);
            final Boolean[] inCell = {false};
            final Boolean[] lbFound = {false};

            try {
                parser.parse(file, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName,  String name, Attributes a) {
                        if (name.equalsIgnoreCase("row")) {
                            Row row = new Row();
                            table.getRows().add(row);
                            row.setRend(a.getValue("rend"));
                        } else if (name.equalsIgnoreCase("cell")) {
                            inCell[0] = true;
                            Cell cell = new Cell();
                            table.getRows().get(table.getRows().size() - 1).getCells().add(cell);

                            String colsAttr = a.getValue("cols");
                            String rowsAttr = a.getValue("rows");

                            if (colsAttr != null) {
                                cell.setCols(Integer.parseInt(colsAttr));
                            }

                            if (rowsAttr != null) {
                                cell.setRows(Integer.parseInt(rowsAttr));
                            }

                            cell.setRend(a.getValue("rend"));
                            cell.setRole(a.getValue("role"));

                        } else if (name.equalsIgnoreCase("measure")) {
                            Row row = table.getRows().get(table.getRows().size() - 1);
                            Cell cell = row.getCells().get(row.getCells().size() - 1);
                            cell.getValue().setMeasure(true);
                            cell.getValue().setMeasureType(a.getValue("type"));
                            cell.getValue().setMeasureUnit(a.getValue("unit"));
                        } else if (name.equalsIgnoreCase("num")) {
                            Row row = table.getRows().get(table.getRows().size() - 1);
                            Cell cell = row.getCells().get(row.getCells().size() - 1);
                            cell.getValue().setNum(true);
                            cell.getValue().setNumType(a.getValue("type"));
                        } else if (name.equalsIgnoreCase("lb")) {
                            lbFound[0] = true;
                        }
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) {
                        if (qName.equalsIgnoreCase("row")) {
                            // nothing to do?
                        } else if (qName.equalsIgnoreCase("cell")) {
                            inCell[0] = false;
                        }
                    }

                    @Override
                    public void characters(char ch[], int start, int length) {
                        if (table.getRows().size() > 0) {
                            Row row = table.getRows().get(table.getRows().size() - 1);

                            if (row.getCells().size() > 0) {
                                Cell cell = row.getCells().get(row.getCells().size() - 1);
                                if (inCell[0]) {
                                    if (lbFound[0]) {
                                        cell.getValue().setValue(cell.getValue().getValue() + PLACEHOLDER_LB);
                                        lbFound[0] = false;
                                    }
                                    cell.getValue().setValue(cell.getValue().getValue() + new String(ch, start, length));
                                }
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

        return table;
    }

}
