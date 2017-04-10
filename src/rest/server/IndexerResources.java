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
		db.store(id, doc);
	}


	@DELETE
	@Path("/{id}")
	public void remove(@PathParam("id") String id){
		db.remove(id);
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> search(@QueryParam("query") String keywords){
		List<String> list = new ArrayList<String>();
		list.add(keywords);
		List<Document> docList = db.search(list);
		list.clear();
		for(int i = 0; i < docList.size(); i++){
			list.addAll(docList.get(i).getKeywords());

		}
		return list;

	}
}
