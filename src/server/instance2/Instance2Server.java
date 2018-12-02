package server.instance2;

import server.instance2.controller.udp.UDPServer;

public class Instance2Server {
	
	public static void main(String[] args) throws Exception {	

		UDPServer compServer = new UDPServer("COMP");
		UDPServer soenServer = new UDPServer("SOEN");
		UDPServer inseServer = new UDPServer("INSE");
		
		compServer.start();
		soenServer.start();
		inseServer.start();

		System.out.println("Insance 2 Server initated");
		
	}
	
}
