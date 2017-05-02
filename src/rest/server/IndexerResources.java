package rest.server;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import api.IndexerService;
import api.ServerConfig;
import api.soap.IndexerAPI;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;
import api.Document;
import api.Endpoint;

import static javax.ws.rs.core.Response.Status.*;


@Path("/indexer")
public class IndexerResources implements IndexerService{

	private Storage db = new LocalVolatileStorage();
	private URI rendezVousUri;

	public IndexerResources(URI rendezVous){
		rendezVousUri = rendezVous;
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void add(@PathParam("id") String id, Document doc){
		if(db.store(id, doc))
			System.err.printf("update: %s <%s>\n", id, doc);
		else throw new WebApplicationException( CONFLICT );
	}


	@DELETE
	@Path("/{id}")
	public void remove(@PathParam("id") String id){

		boolean found = false;

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(rendezVousUri);
		Endpoint[] endpoints = target.path("/contacts")
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(Endpoint[].class);
		List<Endpoint> indexers = Arrays.asList(endpoints);
		for(Endpoint indexer : indexers){

			if(!indexer.getAttributes().get("type").equals("soap")){
				try{
					target = client.target(indexer.getUrl());
					Response response = target.path("/indexer/local/" +id)
							.request()
							.delete();

	
					if(response.getStatus() == 204)
						found = true;

				}catch (ProcessingException e){

				}
			}

			else{
				URL wsURL = null;
				try {
					wsURL = new URL(indexer.getUrl()+"/indexer?wsdl");
				} catch (MalformedURLException e1) {
				}

				QName qname = new QName( IndexerAPI.NAMESPACE, IndexerAPI.NAME);

				try {
				Service service = Service.create( wsURL, qname);

				IndexerAPI indexer1 = service.getPort( IndexerAPI.class );

					if(indexer1.removelocal(id))
						found = true;
				} catch (Exception e) {

				}
			}
		}
		if(!found)
			throw new WebApplicationException( NOT_FOUND );

	}

	@DELETE
	@Path("/local/{id}")
	public void removelocal(@PathParam("id") String id){
		if(!db.remove(id)){
			throw new WebApplicationException( NOT_FOUND );
		}
		else
			System.err.printf("removed: %s \n", id);
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> search(@QueryParam("query") String keywords){
		List<String> list = Arrays.asList(keywords.split("\\+"));
		List<Document> docList = db.search(list);
		list = new ArrayList<String>();
		for(int i = 0; i < docList.size(); i++){
			list.add(docList.get(i).getUrl());
		}
		return list;
	}

	@Override
	public void configure(String secret, ServerConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(String id, String secret, Document doc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(String id, String secret) {
		// TODO Auto-generated method stub
		
	}
}
