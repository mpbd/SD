package rest.server;

import java.util.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.WebApplicationException;
import api.IndexerService;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;
import api.Document;

import static javax.ws.rs.core.Response.Status.*;


@Path("/indexer")
public class IndexerResources implements IndexerService{

	private Storage db = new LocalVolatileStorage();

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
		
		db.remove(id);/*
		ClientConfig config2 = new ClientConfig();
		Client client = ClientBuilder.newClient(config2);
		WebTarget target = client.target(baseURI);
		Endpoint[] endpoints = target.path("/contacts")
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(Endpoint[].class);
				*/
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
}
