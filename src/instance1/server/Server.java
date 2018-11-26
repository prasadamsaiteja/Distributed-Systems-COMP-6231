package instance1.server;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import instance1.corbaInterface.IDCRS;
import instance1.corbaInterface.IDCRSHelper;
import instance1.server.controller.rmi.DCRS;
import instance1.server.controller.socket.UDPServer;
import instance1.server.data.Database;
import instance1.logging.Logger;

public class Server{

	public static void main(String[] args) throws  MalformedURLException, SocketException {
		
		try {
			
			Database database = Database.getInstance();
			Logger.isServer = true;
			
			System.out.print("Enter Department Code: ");					
			database.department = new Scanner(System.in).nextLine().toUpperCase();
		
			if( !(database.department.equals("COMP") || database.department.equals("SOEN") || database.department.equals("INSE"))){
				System.out.println("Invalid Department");
				return;
			}
			
			/*DCRS dcrs = new DCRS();
			Registry registry = LocateRegistry.createRegistry(Database.serverPorts.get(database.department).getKey());
			registry.bind(database.department, dcrs);
			new UDPServer().start(); */
						
			ORB orb = ORB.init(args, null);      
		    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		    rootpoa.the_POAManager().activate();
		 
		    // create servant and register it with the ORB
		    DCRS dcrs = new DCRS();
		    dcrs.setORB(orb); 
		 
		    // get object reference from the servant
		    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(dcrs);
		    IDCRS href = IDCRSHelper.narrow(ref);
		 
		    org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
		    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		 
		    NameComponent path[] = ncRef.to_name(database.department);
		    ncRef.rebind(path, href);
		 
		    System.out.println(database.department + " Server initated \n");
		    new UDPServer().start();
		    
		    // wait for invocations from clients
		    for (;;) { orb.run(); }

		} catch (InvalidName | org.omg.CORBA.ORBPackage.InvalidName | ServantNotActive | WrongPolicy | AdapterInactive | NotFound | CannotProceed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}