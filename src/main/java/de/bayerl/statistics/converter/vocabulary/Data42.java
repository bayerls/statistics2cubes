package de.bayerl.statistics.converter.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


public class Data42 {

	protected static final String PREFIX = "data42";
    protected static final String URI = "http://42-data.org/resource/";

    public static String getPrefix() {
    	return PREFIX;
    }

    public static String getURI() {
          return URI;
    }

    private static Model m = ModelFactory.createDefaultModel();
    
    public static final String DATASET = URI + "Dataset";
    public static final String DSD = URI + "Dsd";
    public static final String OBS_NAME = "Obs";
    
    // Used with PROV
    public static final String IMPORT = URI + "Import";
    public static final String IMPORTER = URI + "Importer";
    
    
    public static final Resource ENTITY = m.createResource(URI + "Entity");
    public static final String COMPONENT = URI + "Component";

}
