package rest.server;

import java.util.*;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import api.Endpoint;
import api.Document;

import static javax.ws.rs.core.Response.Status.*;



public class IndexerResources {
	private List<String> db = new ArrayList<String>();

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> search(@QueryParam("query") String keywords){
		return db;
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void add( @PathParam("id") String id, Document doc ){

	}

	@DELETE
	@Path("/{id}")
	public void remove( @PathParam("id") String id ){

	}


}
