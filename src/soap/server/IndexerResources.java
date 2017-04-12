package soap.server;

import java.util.*;
import javax.jws.WebService;

import api.Document;
import api.soap.IndexerAPI;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;



@WebService(
	serviceName = IndexerAPI.NAME,
	targetNamespace = IndexerAPI.NAMESPACE,
	endpointInterface = IndexerAPI.INTERFACE)


public class IndexerResources implements IndexerAPI{

	private Storage db = new LocalVolatileStorage();


	@Override
	public boolean add(Document doc) throws InvalidArgumentException{
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
