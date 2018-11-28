package server.instance2.ws;

import java.util.Scanner;


import server.instance2.Impl.UDPServer;


public class Server {

	
	
	
	public static void main(String[] args) throws Exception{


		Scanner sc= new Scanner(System.in);

		System.out.println("Enter the department:");
		String dept = sc.next();
		if(dept.equalsIgnoreCase("comp")) {

			UDPServer udpServer = new UDPServer();
			udpServer.udpMain(dept.toUpperCase());
		}
		else if(dept.equalsIgnoreCase("soen")) {

			UDPServer udpServer = new UDPServer();
			udpServer.udpMain(dept.toUpperCase());
		}

		else if(dept.equalsIgnoreCase("inse")) {

			UDPServer udpServer = new UDPServer();
			udpServer.udpMain(dept.toUpperCase());
		}

	}

}
