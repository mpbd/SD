package rest.server;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.glassfish.jersey.client.ClientConfig;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import api.IndexerService;
import api.ServerConfig;
import api.soap.IndexerAPI;
import api.Document;
import api.Endpoint;

import static javax.ws.rs.core.Response.Status.*;

@Path("/indexer")
public class IndexerResources implements IndexerService {

	private URI rendezVousUri;
	private String secret;
	private DBCollection table;
	private DB db;

	public IndexerResources(URI rendezVous, String secret) {
		rendezVousUri = rendezVous;
		this.secret = secret;

		MongoClientURI uri = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/maria?w=2&readPreference=secondary");
		MongoClient mongo = null;
		try {
			mongo = new MongoClient(uri);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		db = mongo.getDB("testDB");
		db.requestStart();
		table = db.getCollection("col");
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> search(@QueryParam("query") String keywords) {

		List<String> list = Arrays.asList(keywords.split("[ \\+]"));
		List<String> results = new ArrayList<String>();
		List <String> temp = new ArrayList<String>();
		/*
		// Pesquisar...
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("keywords", list);
		*/

		DBCursor cursor = table.find();
		DBObject  tempO = null;
		
		while (cursor.hasNext()) {
			tempO =  cursor.next();
			temp = (List<String>) tempO.get("keywords");
			if (temp.containsAll(list)){
				results.add((String) tempO.get("url"));
			}
		}

		return results;
	}

	@PUT
	@Path("/configure")
	@Consumes(MediaType.APPLICATION_JSON)
	public void configure(@QueryParam("secret") String secret, ServerConfig config) {

	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void add(@PathParam("id") String id, @QueryParam("secret") String secret, Document doc) {
		if (secret.equals(this.secret)) {

			// Inserir...

			BasicDBObject document = new BasicDBObject();
			document.put("id", doc.id());
			document.put("url", doc.getUrl());
			document.put("keywords", doc.getKeywords());

			table.insert(document);

			db.requestDone();
			

		} else
			throw new WebApplicationException(FORBIDDEN);
	}

	@DELETE
	@Path("/{id}")
	public void remove(@PathParam("id") String id, @QueryParam("secret") String secret) {
		if (secret.equals(this.secret)) {
			// Pesquisar...
			BasicDBObject toRemove = new BasicDBObject();
			toRemove.put("id", id);
			
			DBCursor cursor = table.find(toRemove);
			
			if (cursor.count() != 0){
				table.remove(toRemove);
			} else throw new WebApplicationException(NOT_FOUND);
				
			
		} else
			throw new WebApplicationException(FORBIDDEN);
	}
}
