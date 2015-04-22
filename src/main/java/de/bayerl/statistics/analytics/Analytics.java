package de.bayerl.statistics.analytics;

import de.bayerl.statistics.TablePrinter;
import de.bayerl.statistics.TeiLoader;
import de.bayerl.statistics.instance.Config;
import de.bayerl.statistics.instance.Conversion;
import de.bayerl.statistics.instance.Preview;
import de.bayerl.statistics.model.Table;
import de.bayerl.statistics.transformer.Transformation;
import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sebastianbayerl on 16/03/15.
 */
public class Analytics {

    public void computeValues() {
        Map<Path, Map<Path, Integer>> filtered = filterFolders();
        List<Integer> sizes = new ArrayList<>();

        for (Path folder : filtered.keySet()) {
            Map<Path, Integer> map = filtered.get(folder);
            sizes.addAll(map.keySet().stream().map(map::get).collect(Collectors.toList()));
        }

        Collections.sort(sizes);
        System.out.println("#link groups " + sizes.size());

        int total = 0;
        Map<Integer, Integer> count = new HashMap<>();
        for (int size : sizes) {
            total += size;
            if (!count.containsKey(size)) {
                count.put(size, 0);
            }
            count.put(size, count.get(size) + 1);
        }

        System.out.println("#documents " + total);

        int last = 0;
        List<Integer> grouped = new ArrayList<>();
        List<Integer> groupSize = new ArrayList<>();
        for (Integer size : sizes) {
            if (size > last) {
                grouped.add(size);
                groupSize.add(count.get(size));
                last = size;
            }
        }

        System.out.println(grouped);
        System.out.println(groupSize);
        System.out.println("#link groups with different size: " + grouped.size());
    }

    public void computeSimilarLinkGroups() {
        Map<Path, Map<Path, Integer>> filtered = filterFolders();
        List<String> titles = new ArrayList<>();

        for (Path folder : filtered.keySet()) {
            //System.out.println("Folder: " + folder);

            for (Path file : filtered.get(folder).keySet()) {
                //System.out.println(filtered.get(folder).get(file) + " - " + file.getFileName().toString());
                Table table = TeiLoader.load(folder.toString() + "/", file.getFileName().toString());
                Conversion conversion = new Preview();

                for (Transformation transformer : conversion.getTransformations()) {
                    table = transformer.transform(table);
                }

                MetaTable metaTable = new MetaTable();
                metaTable.setFile(file.getFileName().toString());
                metaTable.setLinkGroupSize(filtered.get(folder).get(file));
                metaTable.setTitle(table.getRows().get(1).getCells().get(1).getValue().getValue());


                titles.add(table.getRows().get(1).getCells().get(1).getValue().getValue());
            }
        }


        Collections.sort(titles);
        titles.forEach(s -> System.out.println(s));
    }

    public void getPreview() {
        Map<Path, Map<Path, Integer>> filtered = filterFolders();

        for (Path folder : filtered.keySet()) {
            //System.out.println("Folder: " + folder);

            for (Path file : filtered.get(folder).keySet()) {
                //System.out.println(filtered.get(folder).get(file) + " - " + file.getFileName().toString());
                Table table = TeiLoader.load(folder.toString() + "/", file.getFileName().toString());
                Conversion conversion = new Preview();

                for (Transformation transformer : conversion.getTransformations()) {
                    table = transformer.transform(table);
                }

                TablePrinter.printHTML(table, "" + filtered.get(folder).get(file) + "-" + file.getFileName().toString(), conversion);
            }
        }
    }

    private Map<Path, Map<Path, Integer>> filterFolders() {
        Map<Path, Map<Path, Integer>> filteredMap = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(Config.FOLDER_UNZIP))) {
            for (Path folder : stream) {
                if (!folder.getFileName().toString().equals(".DS_Store")) {
                    filteredMap.put(folder, filterRelevant(folder));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filteredMap;
    }

    private Map<Path, Integer> filterRelevant(Path folder) {
        Map<Path, Integer> filtered = new HashMap<>();
        Set<String> done = new HashSet<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                if (name.endsWith(".tei") && !done.contains(name)) {
                    List<String> group = loadLinkGroup(file);
                    int groupSize = group.size();
                    if (groupSize == 0) {
                        groupSize = 1;
                    }
                    filtered.put(file, groupSize);
                    done.addAll(group);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filtered;
    }


    private static List<String> loadLinkGroup(Path path) {
        SAXParserImpl parser = null;
        List<String> links = new ArrayList<>();

        try {
            parser = SAXParserImpl.newInstance(new HashMap());
        } catch (SAXException e) {
            e.printStackTrace();
        }

        if (parser != null) {
            // use the first file in the folder to grab the link group
            File file = path.toFile();

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
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        }

        return links;
    }

}
