package de.bayerl.statistics.persitance;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.riot.Lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by sebastianbayerl on 31/05/15.
 */
public class BigdataEndpoint {

    private static final String STORE  = "http://zaire.dimis.fim.uni-passau.de:8181/bigdata-statistics/sparql";

    private final static String CONTEXT_URI = "?context-uri=";

    private String convertModelToString(Model model) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, Lang.N3.getName());

        return baos.toString();
    }

    public String persist(Model model, ContentTypeRdf contentType, String context) {
        String content = convertModelToString(model);
        //System.out.println(content);
        HttpClient httpClient = HttpClients.createDefault();


        HttpPost httpPost = new HttpPost(STORE + CONTEXT_URI + context);

        httpPost.setHeader("Content-Type", contentType.getContentTypeRdf());

        try {
            httpPost.setEntity(new StringEntity(content));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        String result = "";
        try {
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = IOUtils.toString(entity.getContent(), "UTF-8");
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpPost.releaseConnection();
        }

        return result;
    }
}
