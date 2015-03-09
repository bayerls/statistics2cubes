package de.bayerl.statistics.instance;

import de.bayerl.statistics.transformer.Transformation;

import java.util.List;

public interface Conversion {

    String getFolder();
    List<Transformation> getTransformations();


}
