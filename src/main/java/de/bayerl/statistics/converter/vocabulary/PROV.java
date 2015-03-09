package de.bayerl.statistics.converter.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

public class PROV {

	protected static final String PREFIX = "prov";
    protected static final String URI = "http://www.w3.org/ns/prov#";

    public static String getPrefix() {
    	return PREFIX;
    }

    public static String getURI() {
          return URI;
    }

    private static Model m = ModelFactory.createDefaultModel();
    public static final Property WAS_GENERATED_BY = m.createProperty(URI + "wasGeneratedBy");
    public static final Property WAS_STARTED_BY = m.createProperty(URI + "wasStartedBy");

}
