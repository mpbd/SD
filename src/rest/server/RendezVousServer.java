package rest.server;

import api.Endpoint;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.client.ClientConfig;


public class RendezVousServer {

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if( args.length > 0)
			port = Integer.parseInt(args[0]);

		Map<InetAddress, Long> heartbeat_db = new ConcurrentHashMap<>();


		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new RendezVousResources() );

		ClientConfig config2 = new ClientConfig();
		Client client = ClientBuilder.newClient(config2);
		WebTarget target = client.target(baseUri);

		JdkHttpServerFactory.createHttpServer(baseUri, config);

		InetAddress address = InetAddress.getByName( "228.10.10.10" ) ;
		MulticastSocket socket = new MulticastSocket( 6969 ) ;
		socket.joinGroup( InetAddress.getByName( "228.10.10.10"));


			while(true){

			//RECEBER
			byte[] buffer = new byte[65536];
			DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
			socket.receive( packet );
			String packet_string = new String(packet.getData(),0,packet.getLength());
			InetAddress packet_address = packet.getAddress();
			int packet_port = packet.getPort();

			//DETECÇÃO DE FALHAS NOS INDEXERS
			for(InetAddress key : heartbeat_db.keySet()) {
				long t = System.currentTimeMillis();
				if ((t - heartbeat_db.get(key)) > 16000){
					heartbeat_db.remove(key);

					String hostIP = key.getHostAddress();
					Endpoint endpoint = new Endpoint("http://" + hostIP + ":8080", Collections.emptyMap());
					Response response = target.path("/contacts/" + endpoint.generateId())
										.request()
										.delete();
				}
			}

			//RECEPÇÃO DE HEARTBEATS

			if (packet_string.equals("heartbeat")){
				long current_time = System.currentTimeMillis();
				heartbeat_db.put(packet.getAddress(), current_time);
			} else if (packet_string.equals("rendezvous")){

				//ENVIAR RESPOSTA COM O ENDEREÇO DO RENDEVOUS
				String test = "http://" +  InetAddress.getLocalHost().getHostAddress() + ":" + 8080 +"/contacts";
				byte[] input = test.getBytes();
				DatagramPacket packet2 = new DatagramPacket( input, input.length );
				packet2.setAddress(packet_address);
				packet2.setPort(packet_port);
				socket.send(packet2);
			}

		}
	}
}
