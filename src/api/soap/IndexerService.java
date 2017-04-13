package api.soap;

import java.util.List;


import api.Document;


public interface IndexerService {


List<String> search( String keywords );


void add( Document doc );


void remove(String id );
}
