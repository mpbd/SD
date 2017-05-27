package rest.twitter;

import java.net.*;

import api.*;


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
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


public class ProxyServer {

	static public class InsecureHostnameVerifier implements HostnameVerifier {
		
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

		
		
		URI rendezvous_URI = UriBuilder.fromUri(args[1]).build();

		
		ResourceConfig config = new ResourceConfig();
		config.register(new ProxyResources( args[0], rendezvous_URI));
		JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

		
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();
		WebTarget target = client.target(rendezvous_URI);

		
		String ip = InetAddress.getLocalHost().getHostAddress();
		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("type", "rest");
		Endpoint endpoint = new Endpoint("https://" + ip + ":" + port, map);
		Response response = target.path(endpoint.generateId()).queryParam("secret", args[0]).request()
				.post(Entity.entity(endpoint, MediaType.APPLICATION_JSON));
		
		

		while (true) {
			// ENVIO D0 HEARTBEAT
			Thread.sleep(5000);
			Response heartbeat = target.path("/contacts/heartbeat").request()
					.put(Entity.entity(endpoint, MediaType.APPLICATION_JSON));

		}

	}
}
