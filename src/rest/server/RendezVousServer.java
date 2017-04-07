package rest.server;


import java.net.*;


import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class RendezVousServer {

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if( args.length > 0)
			port = Integer.parseInt(args[0]);

		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new RendezVousResources() );

		JdkHttpServerFactory.createHttpServer(baseUri, config);

		InetAddress address = InetAddress.getByName( "228.10.10.10" ) ;
		MulticastSocket socket = new MulticastSocket( 8080 ) ;
		DatagramSocket socketD = new DatagramSocket( 9000 ) ;


		socket.joinGroup( InetAddress.getByName( "228.10.10.10"));


		System.err.println("\nREST RendezVous Server ready @\n-----------------------\nURI: http://0.0.0.0/\nPort:" + port + "\nIP: " + InetAddress.getLocalHost().getHostAddress() + "\nMulticast Address: 228.10.10.10\nMulticast Port: 8080\n-----------------------\n");
		while(true){

			//RECEBER
			byte[] buffer = new byte[65536];
			DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
			socket.receive( packet );
			String temp =new String(packet.getData());

			//ENVIAR
			String test = "rendezvous reply";
			byte[] input = test.getBytes();
			DatagramPacket packet2 = new DatagramPacket( input, input.length );
			packet2.setAddress(packet.getAddress());
			packet2.setPort(9000);
			System.out.println("\n---------\nRecebi: " + temp + "\nVou enviar: " + test + "\nPara: " + packet.getAddress() + "\n---------\n");
			socketD.send(packet2);
		}
	}
}
