

package api.soap;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebFault;

import api.Document;

@WebService
public interface IndexerAPI {
   
    @WebFault
    class InvalidArgumentException extends Exception {

        private static final long serialVersionUID = 1L;

        public InvalidArgumentException() {
            super("");
        }       
        public InvalidArgumentException(String msg) {
            super(msg);
        }
    }

    static final String NAME="IndexerService";
    static final String NAMESPACE="http://sd2017";
    static final String INTERFACE="api.soap.IndexerAPI";

    /* keywords contains a list of works separated by '+'
     * returns the list of urls of the documents stored in this server that contain all the keywords
     * throws IllegalArgumentException if keywords is null
     */
    @WebMethod
    List<String> search(String keywords) throws InvalidArgumentException;

    /*
     * return true if document was added, false if the document already exists in this server.
     * throws IllegalArgumentException if doc is null
     */
    @WebMethod
    boolean add(Document doc) throws InvalidArgumentException ;

    /*
     * return true if document was removed, false if was not found in the system.
     * throws IllegalArgumentException if id is null
     */
    @WebMethod
    boolean remove(String id) throws InvalidArgumentException ;
    
    @WebMethod
    boolean removelocal(String id) throws InvalidArgumentException;
}
