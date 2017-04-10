package rest.client;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;



import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import api.Endpoint;

public class ListEndpoints {

	public static void main(String[] args) throws IOException {

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		InetAddress address = InetAddress.getByName( "228.10.10.10" ) ;
		MulticastSocket socket = new MulticastSocket();
		DatagramSocket socketD = new DatagramSocket( 9000 ) ;


		//ENVIAR
		String temp = "rendezvous";
		byte[] input = temp.getBytes();
		DatagramPacket packet = new DatagramPacket( input, input.length );
		packet.setAddress(address);
		packet.setPort(8420);
		socket.send(packet);

		//RECEBER
		byte[] buffer = new byte[65536];
		DatagramPacket packet2 = new DatagramPacket( buffer, buffer.length );
		socketD.receive( packet2 );
		String temp2 = new String(packet2.getData());
		System.out.println("\nRecebi do Servidor: " + temp2 + "\ndo IP: " + packet2.getAddress());
		URI baseURI = UriBuilder.fromUri("http:/" + packet2.getAddress() + ":8420" + "/").build();

		System.out.println(baseURI);
		WebTarget target = client.target(baseURI);
		Endpoint[] endpoints = target.path("/contacts")
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(Endpoint[].class);

		System.err.println("\n-------------------\n" + Arrays.asList(endpoints) + "\n-------------------\n" );

		socket.close() ;
	}
}
