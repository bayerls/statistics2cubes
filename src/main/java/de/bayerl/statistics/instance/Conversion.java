package de.bayerl.statistics.instance;

import de.bayerl.statistics.transformer.Transformation;

import java.util.List;

public interface Conversion {

    /**
     * The name of the folder that will hold all data about the conversion process.
     *
     * @return Folder name
     */
    String getFolder();

    /**
     * The list of transformations that will be applied to the table.
     *
     * @return A list of transformations
     */
    List<Transformation> getTransformations();


}
