package rest.server;

import java.net.InetAddress;
import java.util.Collections;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import api.*;

import static javax.ws.rs.core.Response.Status.*;

/**
 * Implementacao do servidor de rendezvous em REST
 */
@Path("/contacts")
public class RendezVousResources implements RendezVousService{

	private Map<String, Endpoint> db = new ConcurrentHashMap<>();
	private Map<String, Long> heartbeat_db = new ConcurrentHashMap<>();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Endpoint[] endpoints() {
		return db.values().toArray( new Endpoint[ db.size() ]);
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void register( @PathParam("id") String id, Endpoint endpoint) {
		System.err.printf("register: %s <%s>\n", id, endpoint);

		if (db.containsKey(id))
			throw new WebApplicationException( CONFLICT );
		else
			db.put(id, endpoint);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(@PathParam("id") String id, Endpoint endpoint) {
		System.err.printf("update: %s <%s>\n", id, endpoint);

		if ( ! db.containsKey(id))
			throw new WebApplicationException( NOT_FOUND );
		else
			db.put(id, endpoint);
	}

	@DELETE
	@Path("/{id}")
	public void unregister(@PathParam("id") String id) {
		if ( ! db.containsKey(id))
			throw new WebApplicationException( NOT_FOUND );
		else
			db.remove(id);
	}


	@PUT
	@Path("/heartbeat")
	@Consumes(MediaType.APPLICATION_JSON)
	public void heartbeat(Endpoint endpoint) {
		//RECEPÇÃO DE HEARTBEATS
		System.out.println("entrou");
		String id = endpoint.generateId();
		long current_time = System.currentTimeMillis();
		heartbeat_db.put(id, current_time);
		//DETECÇÃO DE FALHAS NOS INDEXERS
		for(String key : heartbeat_db.keySet()) {
			long t = System.currentTimeMillis();
			if ((t - heartbeat_db.get(key)) >= 30000){
				heartbeat_db.remove(key);
				db.remove(key);

			}
		}



	}
}



