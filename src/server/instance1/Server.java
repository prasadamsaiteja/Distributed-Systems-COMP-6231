package server.instance1;

import java.util.Scanner;

import generic.Logger;
import server.instance1.controller.socket.UDPServer;
import server.instance1.data.Database;

public class Server{

	public static void main(String[] args) throws Exception {
					
		Database database = Database.getInstance();
		Logger.isServer = true;
		
		System.out.print("Enter Department Code: ");					
		database.department = new Scanner(System.in).nextLine().toUpperCase();
	
		if( !(database.department.equals("COMP") || database.department.equals("SOEN") || database.department.equals("INSE"))){
			System.out.println("Invalid Department");
			return;
		}
		
	    System.out.println(database.department + " Server initated \n");
	    new UDPServer().start();
		
	}

}