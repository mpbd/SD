package api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/indexer")
public interface IndexerService {

@GET
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
List<String> search( @QueryParam("query") String keywords );

@POST
@Path("/{id}")
@Consumes(MediaType.APPLICATION_JSON)
void add( @PathParam("id") String id, Document doc );

@DELETE
@Path("/{id}")
void remove( @PathParam("id") String id );
}
