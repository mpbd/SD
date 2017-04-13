package soap.server;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.jws.WebService;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.glassfish.jersey.client.ClientConfig;

import api.Document;
import api.Endpoint;
import api.soap.IndexerAPI;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;



@WebService(
		serviceName = IndexerAPI.NAME,
		targetNamespace = IndexerAPI.NAMESPACE,
		endpointInterface = IndexerAPI.INTERFACE)


public class IndexerResources implements IndexerAPI{

	private Storage db = new LocalVolatileStorage();
	private URI rendezVousUri;

	public IndexerResources(URI rendezVous){
		rendezVousUri = rendezVous;
	}


	@Override
	public boolean add(Document doc) throws InvalidArgumentException{
		if(doc == null){
			throw new InvalidArgumentException();
		}
		String id = doc.id();
		if(db.store(id, doc)){
			//System.err.printf("update: %s <%s>\n", id, doc);
			return true;
		} else
			return false;
	}


	@Override
	public boolean remove(String id) throws InvalidArgumentException{
		if(id == null){
			throw new InvalidArgumentException();
		}
		
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

			if(indexer.getAttributes().get("type").equals("soap")){

				URL wsURL = null;
				try {
					wsURL = new URL(indexer.getUrl()+"/indexer?wsdl");
				} catch (MalformedURLException e1) {
				}

				QName qname = new QName( NAMESPACE, NAME);

				try{
				Service service = Service.create( wsURL, qname);

				IndexerAPI indexer1 = service.getPort( IndexerAPI.class );
					if(indexer1.removelocal(id))
						found = true;
				}catch(Exception e){

				}
			}
			else{
				try{
					target = client.target(indexer.getUrl());
					Response response = target.path("/indexer/local/" +id)
							.request()
							.delete();
					if(response.getStatus() == 204)
						found = true;
				}catch(ProcessingException e){

				}
			}
		}
		return found;

	}

	public boolean removelocal(String id) throws InvalidArgumentException{
		if(!db.remove(id)){
			return false;
		}
		else
			return true;
	}


	@Override
	public List<String> search(String keywords) throws InvalidArgumentException{
		if(keywords == null){
			throw new InvalidArgumentException();
		}
		
		List<String> list = Arrays.asList(keywords.split("\\+"));
		List<Document> docList = db.search(list);
		list = new ArrayList<String>();
		for(int i = 0; i < docList.size(); i++){
			list.add(docList.get(i).getUrl());
		}
		return list;
	}
}
