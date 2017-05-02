package rest.twitter;


import java.net.URI;
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

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import javax.ws.rs.WebApplicationException;

import api.IndexerService;
import api.ServerConfig;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;
import api.Document;

import static javax.ws.rs.core.Response.Status.*;


@Path("/indexer")
public class ProxyResources implements IndexerService{

	private Storage db = new LocalVolatileStorage();
	private URI rendezVousUri;
	private OAuth10aService service;
	private OAuth1AccessToken accessToken;

	public ProxyResources(URI rendezVous){
		rendezVousUri = rendezVous;
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void add(@PathParam("id") String id, String secret, Document doc){
		throw new WebApplicationException( FORBIDDEN );
	}


	@DELETE
	@Path("/{id}")
	public void remove(@PathParam("id") String id, String secret){

			throw new WebApplicationException( FORBIDDEN );

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
		
		service = new ServiceBuilder().apiKey(config.getApiKey()).apiSecret(config.getApuSecret())
				.build(TwitterApi.instance());
		
		accessToken = new OAuth1AccessToken(config.getToken(), config.getTokenSecret());
		
		
	}
}
