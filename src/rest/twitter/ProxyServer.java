package rest.twitter;


import java.net.*;
import api.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	public static void main(String[] args) throws Exception {
		int port = 8080;
		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();


		InetAddress multicast_address = InetAddress.getByName( "228.10.10.10" ) ;
		MulticastSocket socket = new MulticastSocket( 6970 );

		socket.joinGroup( InetAddress.getByName( "228.10.10.10"));


		//ENVIAR
		String temp = "rendezvous";
		byte[] input = temp.getBytes();
		DatagramPacket packet = new DatagramPacket( input, input.length );
		packet.setAddress(multicast_address);
		packet.setPort(6969);
		socket.send(packet);


		//RECEBER A INFO DO RENDEVOUS
		byte[] buffer = new byte[65536];
		DatagramPacket packet2 = new DatagramPacket( buffer, buffer.length );
		socket.receive( packet2 );

		URI rendezvous_URI = UriBuilder.fromUri("http:/" + packet2.getAddress() + ":8080" + "/").build();



		//CHAMADA AO METODO REST NO RENDEZVOUS PARA REGISTAR ENDPOINT
		ResourceConfig config = new ResourceConfig();
		config.register( new ProxyResources(rendezvous_URI) );

		ClientConfig config2 = new ClientConfig();
		Client client = ClientBuilder.newClient(config2);

		//JdkHttpServerFactory.createHttpServer(baseUri, config);
		JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());
		WebTarget target = client.target(rendezvous_URI);

		String ip = InetAddress.getLocalHost().getHostAddress();
		Map<String,Object> map = new ConcurrentHashMap<String,Object>();
		map.put("type", "rest");
		Endpoint endpoint = new Endpoint("http://" + ip + ":" + port, map);
		Response response = target.path("/contacts/" + endpoint.generateId())
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
