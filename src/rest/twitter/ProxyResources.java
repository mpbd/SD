package rest.twitter;


import java.net.URI;
import java.net.URLEncoder;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
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
		final OAuth1RequestToken requestToken = service.getRequestToken();

		// Obtain the Authorization URL
		System.out.println("A obter o Authorization URL...");
		final String authorizationUrl = service.getAuthorizationUrl(requestToken);
		System.out.println("Necessario dar permissao neste URL:");
		System.out.println(authorizationUrl);
		System.out.println("e copiar o codigo obtido para aqui:");
		System.out.print(">>");

		// Ready to execute operations
		OAuthRequest searchReq = new OAuthRequest(Verb.GET,
				"https://api.twitter.com/1.1/search/tweets.json?q="
						+ URLEncoder.encode(keywords, "UTF-8"));
		service.signRequest(accessToken, searchReq);
		final Response searchRes = service.execute(searchReq);
		System.err.println("REST code:" + searchRes.getCode());
		if (searchRes.getCode() != 200)
			return;
		// System.err.println("REST reply:" + followersRes.getBody());

		JSONParser parser = new JSONParser();
		JSONObject res = (JSONObject) parser.parse(searchRes.getBody());

		JSONArray idStr = (JSONArray) res.get("id_str");
		int count = 0;
		for (Object user : idStr) {
			System.out.println("" + (++count) + " > " + ((JSONObject) user).get("name"));
		}
	}

	@Override
	public void configure(String secret, ServerConfig config) {
		
		service = new ServiceBuilder().apiKey(config.getApiKey()).apiSecret(config.getApuSecret())
				.build(TwitterApi.instance());
		
		accessToken = new OAuth1AccessToken(config.getToken(), config.getTokenSecret());
		
		
	}
}
