package de.bayerl.statistics.persitance;

import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import org.apache.jena.riot.Lang;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class VirtuosoDao {

	
	private final static String PREFIXES = "PREFIX qb: <http://purl.org/linked-data/cube#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX code: <http://42-data.org/resource/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX prov: <http://www.w3.org/ns/prov#> ";
	private final static String QUERY_DATASET_DESCRIPTIONS = PREFIXES + "SELECT DISTINCT ?g ?id ?label ?description WHERE { GRAPH ?g { ?id rdf:type qb:DataSet . ?id dc:date ?date . ?id prov:wasGeneratedBy ?activity . ?activity prov:wasStartedBy ?agent . ?agent rdfs:label ?auth . ?id rdfs:label ?label . ?id rdfs:comment ?description }} ORDER BY DESC(?date)";
//	private final static String ENDPOINT_VIRTUOSO = "http://zaire.dimis.fim.uni-passau.de:8890/sparql";
//	private final static String GENERIC_CONSTRUCT = PREFIXES + "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o }}";
	
	private final static String CLEAR_GRAPH = "CLEAR GRAPH ?g";
	private final static String GET_GRAPH = "SELECT * FROM ?g WHERE { ?s ?p ?o }";
	
	// TODO move to properties
	private final static String ENDPOINT = "jdbc:virtuoso://zaire.dimis.fim.uni-passau.de:1111";
	private final static String USER = "dba";
	private final static String PW = "dba";
	
	private final static int BATCH_SIZE = 1000;
	
//	@Override
//	public List<DatasetDescription> getDatasetsFromUser(String userId) {
//		VirtGraph set = new VirtGraph(ENDPOINT, USER, PW);
//        ParameterizedSparqlString prepareQuery = new ParameterizedSparqlString(QUERY_DATASET_DESCRIPTIONS);
//        prepareQuery.setLiteral("auth", userId, XSDDatatype.XSDstring);
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(prepareQuery.toString(), set);
//		ResultSet results = vqe.execSelect();
//        List<DatasetDescription> descriptions = new LinkedList<DatasetDescription>();
//
//        while (results.hasNext()) {
//            QuerySolution result = results.next();
//            DatasetDescription dd = new DatasetDescription();
//            dd.setNamedGraph(result.get("g").toString());
//            dd.setDatasetId(result.get("id").toString());
//            dd.setLabel(result.get("label").toString());
//            dd.setDescription(result.get("description").toString());
//            descriptions.add(dd);
//        }
//
//        vqe.close();
//
//        return descriptions;
//    }
    
//    @Override
//	public String getGraph(String namedGraph, Lang language) {
//		VirtGraph set = new VirtGraph(ENDPOINT, USER, PW);
//        ParameterizedSparqlString prepareQuery = new ParameterizedSparqlString(GET_GRAPH);
//        prepareQuery.setIri("g", namedGraph);
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(prepareQuery.toString(), set);
//		ResultSet results = vqe.execSelect();
//		Model m = ModelFactory.createDefaultModel();
//
//		while (results.hasNext()) {
//			QuerySolution rs = results.next();
//			Resource s = rs.getResource("s");
//			Property p = m.createProperty(rs.get("p").toString());
//			RDFNode o = rs.get("o");
//			m.add(s, p, o);
//		}
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        m.write(baos, language.getName());
//
//		return baos.toString();
//	}
//
//

	public String deleteCube(String namedGraph) {
		VirtGraph set = new VirtGraph(ENDPOINT, USER, PW);
        ParameterizedSparqlString prepareQuery = new ParameterizedSparqlString(CLEAR_GRAPH);
        prepareQuery.setIri("g", namedGraph);
		VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(prepareQuery.toString(), set);
		vur.exec();

		return null;
	}



	public String importUpload(Model model, String context) {
//        String content = convertModelToString(model);
//        VirtGraph set = new VirtGraph(context, ENDPOINT, USER, PW);
//        Model m = ModelFactory.createDefaultModel();
//        m.read(new StringReader(content), null, Lang.N3.getName());
//        List<Triple> triples = new ArrayList<Triple>();
//        StmtIterator stmtIt = m.listStatements();
//
//        while (stmtIt.hasNext()) {
//            triples.add(stmtIt.nextStatement().asTriple());
//        }
//
//        GraphUtil.add(set, triples);
//
//        return null;


        VirtGraph set = new VirtGraph(context, ENDPOINT, USER, PW);
		StmtIterator stmtIt = model.listStatements();

		int i = 0;
		Model tempModel = ModelFactory.createDefaultModel();

		while (stmtIt.hasNext()) {
			tempModel.add(stmtIt.next());
			i++;

			if (i == BATCH_SIZE || !stmtIt.hasNext()) {
                System.out.println("batch");
                String query = "INSERT INTO ?g {";
				query += convertModelToString(tempModel);
				query += "}";

		        ParameterizedSparqlString prepareQuery = new ParameterizedSparqlString(query);
		        prepareQuery.setIri("g", context);
				VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(prepareQuery.toString(), set);
				vur.exec();
				i = 0;
				tempModel = ModelFactory.createDefaultModel();
			}
		}

		return null;
	}
	
    private String convertModelToString(Model model) {
        StringWriter stringWriter = new StringWriter();
        model.write(stringWriter, "TURTLE");
        
        return stringWriter.toString();
    }

}
