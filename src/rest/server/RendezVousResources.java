package rest.server;

import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import utils.*;
import api.*;

import static javax.ws.rs.core.Response.Status.*;

/**
 * Implementacao do servidor de rendezvous em REST
 */
@Path("/contacts")
public class RendezVousResources implements RendezVousAPI {

	private Map<String, Endpoint> db = new ConcurrentHashMap<>();
	private Map<String, Long> heartbeat_db = new ConcurrentHashMap<>();
	private String secret;
	private static final String PATH = "/sd/rendezvous";
	private Zookeeper zk;

	public RendezVousResources(String secret) {
		this.secret = secret;
		try {
			zk = new Zookeeper("zoo1,zoo2,zoo3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		zk.saveValue(PATH, "");
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Endpoint[] endpoints() {
		List<Endpoint> ls = zk.listValues(PATH + "/");
		int ls_size = ls.size();
		return ls.toArray( new Endpoint[ls_size]);
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void register(@PathParam("id") String id, @QueryParam("secret") String secret, Endpoint endpoint) {
		if (secret.equals(this.secret)) {
			if (!zk.contains(PATH + "/" + id)){
				zk.saveValue(PATH + "/" + id, endpoint);
			} else throw new WebApplicationException(CONFLICT);
		} else
			throw new WebApplicationException(FORBIDDEN);
	}

	
	@DELETE
	@Path("/{id}")
	public void unregister(@PathParam("id") String id, @QueryParam("secret") String secret) {
		if (!db.containsKey(id))
			throw new WebApplicationException(NOT_FOUND);
		else
			db.remove(id);
	}

	@PUT
	@Path("/heartbeat")
	@Consumes(MediaType.APPLICATION_JSON)
	public void heartbeat(Endpoint endpoint) {
		// RECEPÇÃO DE HEARTBEATS
		String id = endpoint.generateId();
		long current_time = System.currentTimeMillis();
		heartbeat_db.put(id, current_time);
		// DETECÇÃO DE FALHAS NOS INDEXERS
		for (String key : heartbeat_db.keySet()) {
			long t = System.currentTimeMillis();
			if ((t - heartbeat_db.get(key)) >= 30000) {
				heartbeat_db.remove(key);
				db.remove(key);

			}
		}

	}
}
