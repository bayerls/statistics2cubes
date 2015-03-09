package de.bayerl.statistics.converter.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class QB {

	protected static final String PREFIX = "qb";
    protected static final String URI = "http://purl.org/linked-data/cube#";

    public static String getPrefix() {
    	return PREFIX;
    }
    
    public static String getURI() {
          return URI;
    }

    private static Model m = ModelFactory.createDefaultModel();

	public static final Resource DSD = m.createResource(URI + "DataStructureDefinition");
	public static final Resource DATASET = m.createResource(URI + "DataSet");
	public static final Resource DIM_PROPERTY = m.createResource(URI + "DimensionProperty");
	public static final Resource MEASURE_PROPERTY = m.createResource(URI + "MeasureProperty");
	public static final Resource ATTRIBUTE_PROPERTY = m.createResource(URI + "AttributeProperty");
	public static final Resource OBSERVATION = m.createResource(URI + "Observation");
	
	public static final Property COMPONENT = m.createProperty(URI + "component");
	public static final Property DIMENSION = m.createProperty(URI + "dimension");
	public static final Property MEASURE = m.createProperty(URI + "measure");
	public static final Property ATTRIBUTE = m.createProperty(URI + "attribute");
	public static final Property STRUCTURE = m.createProperty(URI + "structure");
	public static final Property DATASET_PROPERTY = m.createProperty(URI + "dataSet");  

}

