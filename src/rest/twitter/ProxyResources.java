package rest.twitter;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
public class ProxyResources implements IndexerService {

	private URI rendezVousUri;
	private OAuth10aService service;
	private OAuth1AccessToken accessToken;
	private String secret;

	public ProxyResources(String secret, URI rendezVous) {
		rendezVousUri = rendezVous;
		this.secret = secret;
	}

	@POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
	public void add( @PathParam("id") String id, @QueryParam("secret") String secret, Document doc ){
		throw new WebApplicationException(FORBIDDEN);
	}

	@DELETE
    @Path("/{id}")
    public void remove( @PathParam("id") String id, @QueryParam("secret") String secret ){
		throw new WebApplicationException(FORBIDDEN);

	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> search(@QueryParam("query") String keywords) {
		if(service == null || accessToken == null)
			throw new WebApplicationException(FORBIDDEN);
		System.out.println("\nESTOU AQUI\n");

		String[] temp = keywords.split("[ \\+]");
		String workedKeyword = temp[0];
		for(int i = 1;i < temp.length; i++){
			workedKeyword += "+" + temp[i];
		}
		System.out.println(workedKeyword);

		System.out.println("\nESTOU AQUI  2\n");

		List<String> list = new ArrayList<String>();

		try {

			// Ready to execute operations
			OAuthRequest searchReq = new OAuthRequest(Verb.GET,
					"https://api.twitter.com/1.1/search/tweets.json?q=" + URLEncoder.encode(workedKeyword, "UTF-8"));
			service.signRequest(accessToken, searchReq);
			System.out.println("\nESTOU AQUI  3\n");
			final Response searchRes = service.execute(searchReq);
			System.err.println("REST code:" + searchRes.getCode());
			if (searchRes.getCode() != 200){
				System.err.println("REST reply:");
				return list;
			}

			JSONParser parser = new JSONParser();
			JSONObject res = (JSONObject) parser.parse(searchRes.getBody());

			JSONArray idStr = (JSONArray) res.get("id_str");
			for (Object user : idStr) {
				list.add("https://www.twitter.com/statuses/"+ user);
			}

		} catch (IOException | InterruptedException | ExecutionException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;

	}

	@PUT
    @Path("/configure")
    @Consumes(MediaType.APPLICATION_JSON)
    public void configure( @QueryParam("secret") String secret, ServerConfig config) {

		if(this.secret.equals(secret)){
		service = new ServiceBuilder().apiKey(config.getApiKey()).apiSecret(config.getApiSecret())
				.build(TwitterApi.instance());

		accessToken = new OAuth1AccessToken(config.getToken(), config.getTokenSecret());
		}
		else throw new WebApplicationException(FORBIDDEN);
	}
}
