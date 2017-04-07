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
		MulticastSocket socket = new MulticastSocket( 8080 );
		DatagramSocket socketD = new DatagramSocket( 9000 ) ;

		socket.joinGroup( InetAddress.getByName( "228.10.10.10"));
		System.err.println("\nREST Indexer Server ready @\n-----------------------\nURI: http://0.0.0.0/\nPort:" + port + "\nIP: " + InetAddress.getLocalHost().getHostAddress() + "\nMulticast Address: 228.10.10.10\nMulticast Port: 8080\n-----------------------\n");

		//ENVIAR
		String temp = "rendezvous";
		byte[] input = temp.getBytes();
		DatagramPacket packet = new DatagramPacket( input, input.length );
		packet.setAddress(address);
		packet.setPort(8080);
		socket.send(packet);
		System.out.println("\n-------\nMandei: " + temp + "\nDe: " + InetAddress.getLocalHost().getHostAddress() + "\nPara: " + address + "\n-------\n");

		//RECEBER
		System.out.println("\nVou ficar à espera do servidor...\n");
		byte[] buffer = new byte[65536];
		DatagramPacket packet2 = new DatagramPacket( buffer, buffer.length );
		socketD.receive( packet2 );
		String temp2 = new String(packet2.getData());
		System.out.println("\nRecebi do Servidor: " + temp2 + "\ndo IP: " + packet2.getAddress());
		URI baseURI = UriBuilder.fromUri("http:/" + packet2.getAddress() + ":8080" + "/").build();

		System.out.println(baseURI);
		WebTarget target = client.target(baseURI);


		Endpoint endpoint = new Endpoint("http://" + args[0], Collections.emptyMap());
		Response response = target.path("/contacts/" + endpoint.generateId())
							.request()
							.post( Entity.entity( endpoint, MediaType.APPLICATION_JSON));

	}
}
