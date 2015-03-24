package de.bayerl.statistics.converter.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Created by sebastianbayerl on 24/03/15.
 */
public abstract class LocalNS {
    public abstract String getPrefix();
    public abstract String getURI();

    public final String DATASET = getURI() + "Dataset";
    public final String DSD = getURI() + "Dsd";
    public final String OBS_NAME = "Obs";

    // Used with PROV
    public final String IMPORT = getURI() + "Import";
    public final String IMPORTER = getURI() + "Importer";

    private static Model m = ModelFactory.createDefaultModel();
    public final Resource ENTITY = m.createResource(getURI() + "Entity");
    public final String COMPONENT = getURI() + "Component";



}
