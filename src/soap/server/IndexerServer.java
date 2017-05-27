package soap.server;


import java.net.*;
import api.*;
import rest.server.IndexerServer.InsecureHostnameVerifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;


import org.glassfish.jersey.client.ClientConfig;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

@SuppressWarnings("restriction")
public class IndexerServer {

	static public class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
	
	public static void main(String[] args) throws Exception {
		int port = 8080;
		String baseUri = String.format("https://0.0.0.0:%d/indexer", port);
		String secret = args[0];
		
		URI rendezvous_URI = UriBuilder.fromUri(args[1]).build();
		String ip = InetAddress.getLocalHost().getHostAddress();
		HttpsConfigurator configurator = new HttpsConfigurator(SSLContext.getDefault());
		HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(ip, port), -1);
		httpsServer.setHttpsConfigurator(configurator);
		HttpContext httpContext = httpsServer.createContext("/indexer");
		httpsServer.start();
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();

		javax.xml.ws.Endpoint ep = javax.xml.ws.Endpoint.create( new IndexerResources(rendezvous_URI, secret));
		ep.publish(httpContext);
		WebTarget target = client.target(rendezvous_URI);

		Map<String,Object> map = new ConcurrentHashMap<String,Object>();
		map.put("type", "soap");
		Endpoint endpoint = new Endpoint("https://" + ip + ":" + port, map);
		Response response = target.path(endpoint.generateId())
							.queryParam("secret", secret)
							.request()
							.post( Entity.entity(endpoint, MediaType.APPLICATION_JSON));




		while (true){
			//ENVIO D0 HEARTBEAT
			Thread.sleep(5000);
			Response heartbeat = target.path("/contacts/heartbeat")
					.request()
					.put( Entity.entity(endpoint, MediaType.APPLICATION_JSON));
		}

	}
}
