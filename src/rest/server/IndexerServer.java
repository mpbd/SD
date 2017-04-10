package rest.server;


import java.net.*;
import api.Endpoint;
import java.util.Collections;

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

	public static void main(String[] args) throws Exception {
		int port = 8080;

		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new IndexerResources() );

		ClientConfig config2 = new ClientConfig();
		Client client = ClientBuilder.newClient(config2);

		JdkHttpServerFactory.createHttpServer(baseUri, config);

		InetAddress address = InetAddress.getByName( "228.10.10.10" ) ;
		MulticastSocket socket = new MulticastSocket( 8420 );


		socket.joinGroup( InetAddress.getByName( "228.10.10.10"));


		//ENVIAR
		String temp = "rendezvous";
		byte[] input = temp.getBytes();
		DatagramPacket packet = new DatagramPacket( input, input.length );
		packet.setAddress(address);
		packet.setPort(8080);
		socket.send(packet);


		//RECEBER
		byte[] buffer = new byte[65536];
		DatagramPacket packet2 = new DatagramPacket( buffer, buffer.length );
		socket.receive( packet2 );
		URI baseURI = UriBuilder.fromUri("http:/" + packet2.getAddress() + ":8080" + "/").build();

		WebTarget target = client.target(baseURI);

		String ip = InetAddress.getLocalHost().getHostAddress();
		Endpoint endpoint = new Endpoint("http://" + ip, Collections.emptyMap());
		Response response = target.path("/contacts/" + endpoint.generateId())
							.request()
							.post( Entity.entity( endpoint, MediaType.APPLICATION_JSON));


		String hb = "heartbeat";
		byte[] input_hb = hb.getBytes();
		DatagramPacket packet_hb = new DatagramPacket( input_hb, input_hb.length );
		packet_hb.setAddress(address);
		packet_hb.setPort(8080);

		while (true){
			//ENVIO D0 HEARTBEAT
			//Thread.sleep(5000);
			//socket.send(packet_hb);
			buffer = new byte[65536];
			packet2 = new DatagramPacket( buffer, buffer.length );
			socket.receive( packet2 );
			Response response = target.path("/indexer/" + endpoint.generateId())
								.request()
								.post( Entity.entity( endpoint, MediaType.APPLICATION_JSON));
		}

	}
}
