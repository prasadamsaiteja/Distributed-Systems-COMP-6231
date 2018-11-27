package server.instance1;

import generic.Logger;
import server.instance1.controller.socket.UDPServer;

public class Server{

	public static void main(String[] args) throws Exception {
					
		Logger.isServer = true;
		
	    UDPServer compUDPServer = new UDPServer("COMP");
	    compUDPServer.setName("COMP");
	    compUDPServer.start();
	    
	    UDPServer soenUDPServer = new UDPServer("SOEN");
	    soenUDPServer.setName("SOEN");
	    soenUDPServer.start();
	    
	    UDPServer inseUDPServer = new UDPServer("INSE");
	    inseUDPServer.setName("INSE");
	    inseUDPServer.start();
		
		System.out.println("Server initated");
	}

}