package de.bayerl.statistics.persitance;

/**
 * Created with IntelliJ IDEA.
 * User: Basti
 * Date: 12/02/14
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
public enum ContentTypeRdf {

    RDF_XML("application/rdf+xml"), N3("text/rdf+n3"), TURTLE("application/x-turtle");

    private String contentTypeRdf;

    private ContentTypeRdf(String contentTypeRdf) {
        this.contentTypeRdf = contentTypeRdf;
    }

    public String getContentTypeRdf() {
        return contentTypeRdf;
    }
}
