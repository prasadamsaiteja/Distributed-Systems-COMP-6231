package server.instance2.ws;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;


import server.instance2.Impl.UDPServer;
import server.instance2.Impl.UDPServerThreads;
import sun.security.jca.GetInstance;


public class ServerThreads {

	
	
	
	public static void main(String[] args) throws Exception{


		getInstance("COMP");
		getInstance("SOEN");
		getInstance("INSE");


	}

	public static void getInstance(String dept) throws FileNotFoundException, IOException {

		if(dept.equalsIgnoreCase("comp")) {

			UDPServerThreads udpServer = new UDPServerThreads(dept);
			udpServer.start();
		}
		else if(dept.equalsIgnoreCase("soen")) {

			UDPServerThreads udpServer = new UDPServerThreads("SOEN");
			udpServer.start();
		}

		else if(dept.equalsIgnoreCase("inse")) {

			UDPServerThreads udpServer = new UDPServerThreads("INSE");
			udpServer.start();
		}
	}
}
