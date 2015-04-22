package de.bayerl.statistics.analytics;


import de.bayerl.statistics.instance.Config;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by sebastianbayerl on 16/03/15.
 */
public class TeiUnzipper {

    public static void unzipAll() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(Config.FOLDER_ZIP))) {
            stream.forEach(entry -> unzip(entry));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void unzip(Path path) {
        if (!path.getFileName().toString().equals(".DS_Store")) {
            try {
                ZipFile zipFile = new ZipFile(path.toString());
                zipFile.extractAll(Config.FOLDER_UNZIP);
            } catch (ZipException e) {
                System.out.println(path.toString());
                e.printStackTrace();
            }
        }
    }

}
