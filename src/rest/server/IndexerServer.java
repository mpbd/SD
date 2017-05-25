package rest.server;

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

public class IndexerServer {

	static public class InsecureHostnameVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();
		String secret = args[0];

		InetAddress multicast_address = InetAddress.getByName("228.10.10.10");
		MulticastSocket socket = new MulticastSocket(6970);

		socket.joinGroup(InetAddress.getByName("228.10.10.10"));

		// ENVIAR
		String temp = "rendezvous";
		byte[] input = temp.getBytes();
		DatagramPacket packet = new DatagramPacket(input, input.length);
		packet.setAddress(multicast_address);
		packet.setPort(6969);
		socket.send(packet);

		// RECEBER A INFO DO RENDEVOUS
		byte[] buffer = new byte[65536];
		DatagramPacket packet2 = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet2);

		URI rendezvous_URI = UriBuilder.fromUri("https://" + packet2.getAddress() + ":8080" + "/").build();

		// CHAMADA AO METODO REST NO RENDEZVOUS PARA REGISTAR ENDPOINT
		ResourceConfig config = new ResourceConfig();
		config.register(new IndexerResources(rendezvous_URI, secret));
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();

		JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());
		WebTarget target = client.target(rendezvous_URI);

		String ip = InetAddress.getLocalHost().getHostAddress();
		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("type", "rest");
		Endpoint endpoint = new Endpoint("https://" + ip + ":" + port, map);
		Response response = target.path("/contacts/" + endpoint.generateId())
				.request()
				.post(Entity.entity(endpoint, MediaType.APPLICATION_JSON));

		while (true) {
			// ENVIO D0 HEARTBEAT
			Thread.sleep(5000);
			Response heartbeat = target.path("/contacts/heartbeat").request()
					.put(Entity.entity(endpoint, MediaType.APPLICATION_JSON));

		}

	}

}
