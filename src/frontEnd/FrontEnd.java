package frontEnd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import utils.Config;
import utils.corbaInterface.IDCRS;
import utils.corbaInterface.IDCRSHelper;

public class FrontEnd {

	static DatagramSocket datagramSocket;
	
	public static void main(String[] args) throws SocketException, FileNotFoundException, IOException {
		
		datagramSocket = new DatagramSocket(Config.getConfig("FRONTEND_PORT"));
		datagramSocket.setSoTimeout(5000);
		
		Thread comp = new Thread(new FrontEndThread(args, "COMP"));
		Thread soen = new Thread(new FrontEndThread(args, "SOEN"));
		Thread inse = new Thread(new FrontEndThread(args, "INSE"));
		
		comp.start();
		soen.start();
		inse.start();
		
		System.out.println("FrontEnd initated");
		
	}
	
	public static class FrontEndThread implements Runnable {

		private String[] args;
		private String serverName;
		
		public FrontEndThread(String[] args, String serverName){
			this.args = args;
			this.serverName = serverName;
		}
		
		@Override
		public void run() {
			initFrontEndEngine(args, serverName);
		}
		
	}
	
	public static void initFrontEndEngine(String[] args, String serverName) {
		
		try{
			ORB orb = ORB.init(args, null);      
		    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		    rootpoa.the_POAManager().activate();
		 
		    // create servant and register it with the ORB
		    FrontEndEngine frontEndEngine = new FrontEndEngine(serverName);
		    frontEndEngine.setORB(orb); 
		 
		    // get object reference from the servant
		    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontEndEngine);
		    IDCRS href = IDCRSHelper.narrow(ref);
		 
		    org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
		    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		 
		    NameComponent path[] = ncRef.to_name(serverName);
		    ncRef.rebind(path, href);
		 
		    // wait for invocations from clients
		    for (;;) { orb.run(); }
		    
		}
		
		catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
}