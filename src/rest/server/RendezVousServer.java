package rest.server;


import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;


import api.Endpoint;

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
		MulticastSocket socket = new MulticastSocket( 8080 ) ;



		socket.joinGroup( InetAddress.getByName( "228.10.10.10"));
		System.out.println("\n\n\n\n---------------\nRendezVousServer\n---------------\n\n\n\n");

			while(true){

			//RECEBER
			byte[] buffer = new byte[65536];
			DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
			socket.receive( packet );

			//DETECÇÃO DE FALHAS
			for(InetAddress key : heartbeat_db.keySet()) {
				long t = System.currentTimeMillis();
				if ((t - heartbeat_db.get(key)) > 16000){
					heartbeat_db.remove(key);
					String hostIP = key.getHostAddress();
					Endpoint endpoint = new Endpoint("http://" + hostIP, Collections.emptyMap());

					Response response = target.path("/contacts/" + endpoint.generateId())
										.request()
										.delete();
				}
			}

			//RECEPÇÃO DE HEARTBEATS
			String temp =new String(packet.getData(),0,packet.getLength());
			if (temp.equals("heartbeat")){
				long current_time = System.currentTimeMillis();
				heartbeat_db.put(packet.getAddress(), current_time);
				System.out.println(heartbeat_db);
			}

			//ENVIAR RESPOSTA COM O ENDEREÇO DO RENDEVOUS
			String test = "http://" +  InetAddress.getLocalHost().getHostAddress() + ":" + 8080 +"/contacts";
			byte[] input = test.getBytes();
			DatagramPacket packet2 = new DatagramPacket( input, input.length );
			packet2.setAddress(packet.getAddress());
			packet2.setPort(packet.getPort());
			socket.send(packet2);

		}
	}
}
