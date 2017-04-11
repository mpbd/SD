package soap.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.jws.WebService;

import api.Document;
import api.SOAPIndexerService;
import sys.*;
import utils.*;
import api.IndexerService;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;
import api.Document;

import static javax.ws.rs.core.Response.Status.*;



@WebService(
	serviceName = SOAPIndexerService.NAME,
	targetNamespace = SOAPIndexerService.NAMESPACE,
	endpointInterface = SOAPIndexerService.INTERFACE)


public class IndexerResources implements SOAPIndexerService{

	private Storage db = new LocalVolatileStorage();


	@Override
	public boolean add(Document doc) throws IllegalArgumentException{
		String id = doc.id();
		if(db.store(id, doc)){
			//System.err.printf("update: %s <%s>\n", id, doc);
			return true;
		} else
				return false;
	}



	@Override
	public boolean remove(String id) throws IllegalArgumentException{
		if(db.remove(id)){
			//System.err.printf("update: %s <%s>\n", id, doc);
			return true;
		} else
				return false;
	}


	@Override
	public List<String> search(String keywords){
		List<String> list = Arrays.asList(keywords.split("\\+"));
		List<Document> docList = db.search(list);
		list = new ArrayList<String>();
		for(int i = 0; i < docList.size(); i++){
			list.add(docList.get(i).getUrl());
		}
		return list;
	}
}
