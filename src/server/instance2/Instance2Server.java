package server.instance2;

import java.io.FileNotFoundException;
import java.io.IOException;

import server.instance2.dcrsimpl.UDPServerThreads;

public class Instance2Server {

	public static void main(String[] args) throws Exception{


		getInstance("COMP");
		getInstance("SOEN");
		getInstance("INSE");


	}

	public static void getInstance(String dept) throws FileNotFoundException, IOException {

		if(dept.equalsIgnoreCase("comp")) {

			UDPServerThreads udpServer = new UDPServerThreads("COMP");
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
