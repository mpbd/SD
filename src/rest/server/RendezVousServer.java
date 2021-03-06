package rest.server;


import java.net.*;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;



public class RendezVousServer {

	public static void main(String[] args) throws Exception {
		int port = 8080;
		String secret = args[0];

		URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new RendezVousResources(secret) );
		JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());
		
		//LIGACAO A MULTICAST SOCKET
		InetAddress address = InetAddress.getByName( "228.10.10.10" ) ;
		MulticastSocket socket = new MulticastSocket( 6969 ) ;
		socket.joinGroup( InetAddress.getByName( "228.10.10.10"));

		System.err.println("SSL REST RendezVous Server ready @ " + baseUri + " : host :" + InetAddress.getLocalHost().getHostAddress());
		while(true){

			//RECEBER
			byte[] buffer = new byte[65536];
			DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
			socket.receive( packet );
			String packet_string = new String(packet.getData(),0,packet.getLength());
			InetAddress packet_address = packet.getAddress();
			int packet_port = packet.getPort();
			System.out.println("Aqui esta-------> " + packet_string);

			if (packet_string.equals("rendezvous")){
				//ENVIAR RESPOSTA COM O ENDEREÇO DO RENDEVOUS
				String test = "https://" +  InetAddress.getLocalHost().getHostAddress() + ":" + 8080 +"/contacts";
				byte[] input = test.getBytes();
				DatagramPacket packet2 = new DatagramPacket( input, input.length );
				packet2.setAddress(packet_address);
				packet2.setPort(packet_port);
				socket.send(packet2);
		}
		}

	}

}
