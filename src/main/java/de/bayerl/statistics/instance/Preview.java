package de.bayerl.statistics.instance;

import de.bayerl.statistics.transformer.ResolveLinebreaks;
import de.bayerl.statistics.transformer.Transformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianbayerl on 16/03/15.
 */
public class Preview implements Conversion {
    @Override
    public String getFolder() {
        return "";
    }

    @Override
    public List<Transformation> getTransformations() {
        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new ResolveLinebreaks());

        return transformations;
    }
}
