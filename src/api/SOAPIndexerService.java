package api;


import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import api.Document;

@WebService
public interface SOAPIndexerService {

	final String NAME = "IndexerService";
	final String NAMESPACE = "http://sd2017";
	final String INTERFACE = "api.SOAPIndexerService";


	@WebMethod
	List<String> search(String keywords) throws IllegalArgumentException;


	@WebMethod
	boolean add(Document doc) throws IllegalArgumentException;

	@WebMethod
	boolean remove(String id) throws IllegalArgumentException;
}
