package soap.server;


import java.net.*;
import api.*;
import utils.*;
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
		String baseUri = String.format("http://0.0.0.0:%d/indexer", port);
		ClientConfig config2 = new ClientConfig();
		Client client = ClientBuilder.newClient(config2);

		javax.xml.ws.Endpoint.publish(baseUri, new IndexerResources());


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
		WebTarget target = client.target(rendezvous_URI);

		String ip = InetAddress.getLocalHost().getHostAddress();
		Endpoint endpoint = new Endpoint("http://" + ip + ":" + port, Collections.emptyMap());
		Response response = target.path("/contacts/" + endpoint.generateId())
							.request()
							.post( Entity.entity(endpoint, MediaType.APPLICATION_JSON));


		//DEFINICAO DO HEARTBEAT
		String hb = "heartbeat";
		byte[] input_hb = hb.getBytes();
		DatagramPacket packet_hb = new DatagramPacket( input_hb, input_hb.length );
		packet_hb.setAddress(multicast_address);
		packet_hb.setPort(6969);

		while (true){
			//ENVIO D0 HEARTBEAT
			Thread.sleep(5000);
			socket.send(packet_hb);
		}

	}
}
