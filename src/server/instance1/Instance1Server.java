package server.instance1;

import server.instance1.controller.socket.UDPServer;
import utils.Logger;

public class Instance1Server{

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
		
		System.out.println("Insance 1 Server initated");
	}

}