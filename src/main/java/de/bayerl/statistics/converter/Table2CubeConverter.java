package de.bayerl.statistics.converter;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import de.bayerl.statistics.converter.vocabulary.Data42;
import de.bayerl.statistics.converter.vocabulary.PROV;
import de.bayerl.statistics.converter.vocabulary.QB;
import de.bayerl.statistics.converter.vocabulary.VA;
import de.bayerl.statistics.model.Cell;
import de.bayerl.statistics.model.Header;
import de.bayerl.statistics.model.Row;
import de.bayerl.statistics.model.Table;

import java.util.*;


public class Table2CubeConverter {

    private static final String RELATION = "https://github.com/bayerls/statistics2cubes";
    private Table table;
    private Model model;
    private String id;
    private final static String VERSION = "codeCube/1.2";

    public Table2CubeConverter(Table table) {
        this.id = UUID.randomUUID().toString();
        this.table = table;
        this.model = ModelFactory.createDefaultModel();

        setNamespaces();
    }

    private void setNamespaces() {
        model.setNsPrefix("dc", DC.getURI());
        model.setNsPrefix(QB.getPrefix(), QB.getURI());
        model.setNsPrefix(Data42.getPrefix(), Data42.getURI());
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix(VA.getPrefix(), VA.getURI());
        model.setNsPrefix(PROV.getPrefix(), PROV.getURI());
    }

    public Model convert() {
        Resource ds = createDataset();
        addProvenanceInformation(ds);
        List<Property> headers = createDataStructureDefinition(ds);
        createObservations(headers, ds);

        return model;
    }

    private Resource createDataset() {
        Resource ds = model.createResource(Data42.DATASET + "-" + id);
        ds.addProperty(RDF.type, QB.DATASET);
        ds.addProperty(RDFS.label, table.getMetadata().getLabel());
        ds.addProperty(RDFS.comment, table.getMetadata().getDescription());
        ds.addProperty(DC.format, VERSION);
        ds.addProperty(DC.relation, RELATION);

        for (String source : table.getMetadata().getSources()) {
            ds.addProperty(DC.source, source);
        }

        Literal l = model.createTypedLiteral(GregorianCalendar.getInstance());
        ds.addProperty(DC.date, l);

        return ds;
    }

    private void addProvenanceInformation(Resource dataset) {
        Resource importerAgent = model.createResource(Data42.IMPORTER + "-" + id);
        importerAgent.addLiteral(RDFS.label, table.getMetadata().getImporter());

        Resource importActivity = model.createResource(Data42.IMPORT + "-" + id);
        importActivity.addProperty(PROV.WAS_STARTED_BY, importerAgent);

        dataset.addProperty(PROV.WAS_GENERATED_BY, importActivity);
    }

    private int getMeasureCount() {
        Row row = table.getRows().get(0);
        int measures = 0;

        for (Cell cell : row.getCells()) {
            if (cell.getRole().equals("data")) {
                measures++;
            } else {
                break;
            }
        }

        return measures;
    }

    private List<Property> createDataStructureDefinition(Resource ds) {
        List<Property> headerProperties = new ArrayList<>();
        List<Header> headers = table.getHeaders();

        Resource dsd = model.createResource(Data42.DSD + "-" + id);
        ds.addProperty(QB.STRUCTURE, dsd);
        dsd.addProperty(RDF.type, QB.DSD);

        for (int i = 0; i < headers.size(); i++) {
            Header header = headers.get(i);
            Property p = model.createProperty(header.getUrl());
            headerProperties.add(p);
            p.addProperty(RDF.type, RDF.Property);
            p.addProperty(RDFS.label, header.getLabel());
            p.addProperty(RDFS.subPropertyOf, model.createProperty(header.getRange()));

            if (i < getMeasureCount()) {
                p.addProperty(RDF.type, QB.MEASURE_PROPERTY);
                dsd.addProperty(QB.COMPONENT, model.createResource().addProperty(QB.MEASURE, p));
            } else {
                p.addProperty(RDF.type, QB.DIM_PROPERTY);
                dsd.addProperty(QB.COMPONENT, model.createResource().addProperty(QB.DIMENSION, p));
            }
        }

        return headerProperties;
    }

    private HashMap<Integer, HashMap<String, Resource>> createEntities() {
        HashMap<Integer, HashMap<String, Resource>> entities = new HashMap<>();

        for (int i = getMeasureCount(); i < table.getRows().get(0).getCells().size(); i++) {
            entities.put(i, new HashMap<>());
        }

        for (int i = getMeasureCount(); i < table.getRows().get(0).getCells().size(); i++) {
            HashMap<String, Resource> dimEntities = entities.get(i);
            for (int y = 0; y < table.getRows().size(); y++) {
                Cell cell = table.getRows().get(y).getCells().get(i);
                String id = UUID.randomUUID().toString();
                Resource res = model.createResource(Data42.ENTITY + "_" + id);

                // TODO use more data types
                XSDDatatype xsdDatatype = XSDDatatype.XSDstring;
                Literal literal = model.createTypedLiteral(cell.getValue().getValue(), xsdDatatype);
                res.addLiteral(RDFS.label, literal);
                res.addProperty(RDFS.isDefinedBy, model.createResource(cell.getValue().getUrl()));
                res.addProperty(RDF.type, Data42.ENTITY);
                dimEntities.put(cell.getValue().getValue(), res);
            }
        }

        return entities;
    }

    private void createObservations(List<Property> headers, Resource ds) {
        HashMap<Integer, HashMap<String, Resource>> entities = createEntities();
        String obsPrefix = Data42.DATASET + "-" + id + "/" + Data42.OBS_NAME + "-";

        for (Row row : table.getRows()) {
            Resource obs = model.createResource(obsPrefix + UUID.randomUUID()).addProperty(RDF.type, QB.OBSERVATION);
            obs.addProperty(QB.DATASET_PROPERTY, ds);

            for (int i = 0; i < row.getCells().size(); i++) {
                Cell cell = row.getCells().get(i);

                if (i < getMeasureCount()) {
                    // TODO use more data types
                    XSDDatatype xsdDatatype = XSDDatatype.XSDdouble;
                    Literal literal = model.createTypedLiteral(cell.getValue().getValue(), xsdDatatype);
                    obs.addProperty(headers.get(i), literal);
                } else {
                    Resource entity = entities.get(i).get(cell.getValue().getValue());
                    obs.addProperty(headers.get(i), entity);
                }
            }
        }
    }

}
