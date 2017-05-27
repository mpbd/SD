package soap.server;


import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
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

import api.Document;
import api.Endpoint;
import api.ServerConfig;
import api.soap.IndexerService;
import api.soap.IndexerService.InvalidArgumentException;
import api.soap.IndexerService.SecurityException;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;



@WebService(
		serviceName = IndexerService.NAME,
		targetNamespace = IndexerService.NAMESPACE,
		endpointInterface = IndexerService.INTERFACE)


public class IndexerResources implements IndexerService{

	private URI rendezVousUri;
	private String secret;
	private DBCollection table;
	private DB db;

	public IndexerResources(URI rendezVous, String secret){
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

	@WebMethod
	public boolean add(Document doc, String secret) throws InvalidArgumentException, SecurityException{
		if(doc == null){
			throw new InvalidArgumentException();
		}
		if (secret.equals(this.secret)) {

			BasicDBObject toSearch = new BasicDBObject();
			toSearch.put("id", doc.id());
			
			DBCursor cursor = table.find(toSearch);
			
			if (cursor.count() == 0){
				// Inserir...
			BasicDBObject document = new BasicDBObject();
			document.put("id", doc.id());
			document.put("url", doc.getUrl());
			document.put("keywords", doc.getKeywords());

			table.insert(document);

			db.requestDone();
			return true;
			}
			else 
				return false;
		}else
			throw new SecurityException();
			
	}

	@WebMethod
	public boolean remove(String id, String secret) throws InvalidArgumentException, SecurityException{
		if (secret.equals(this.secret)) {
			// Pesquisar...
			BasicDBObject toRemove = new BasicDBObject();
			toRemove.put("id", id);
			
			DBCursor cursor = table.find(toRemove);
			
			if (cursor.count() != 0){
				table.remove(toRemove);
				return true;
			} else 
				return false;
				
			
		} else
			throw new SecurityException();
	}

	


	@WebMethod
	public List<String> search(String keywords) throws InvalidArgumentException{
		if(keywords == null){
			throw new InvalidArgumentException();
		}

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


	@WebMethod
	public void configure(String secret, ServerConfig config) throws InvalidArgumentException, SecurityException {
		// TODO Auto-generated method stub
		
	}

}
