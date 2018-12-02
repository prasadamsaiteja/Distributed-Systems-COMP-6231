/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3;

import java.io.IOException;

import server.instance3.remoteObject.EnrollmentImpl;
import server.instance3.remoteObject.EnrollmentInterface;
import server.instance3.util.Department;
import utils.Config;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target=
 *      "_blank">Profile</a>
 */
public class Instance3Server {

	private static EnrollmentInterface compServer;
	private static EnrollmentInterface soenServer;
	private static EnrollmentInterface inseServer;

	
	public static void main(String[] args) throws IOException {
		getInstance("COMP");
		getInstance("SOEN");
		getInstance("INSE");
		
		System.out.println("Insance 3 Server initated");
	}
	
	public static EnrollmentInterface getInstance(String serverName) throws IOException {

		if (serverName.equalsIgnoreCase("COMP")) {

			if (compServer == null) {
				compServer = new EnrollmentImpl(Department.COMP.toString(),"COMP_Server.log");
				startUDPServer(compServer, Config.getConfig("INSTANCE3_COMP_PORT"));
			}
			return compServer;

		} else if (serverName.equalsIgnoreCase("SOEN")) {

			if (soenServer == null) {
				soenServer = new EnrollmentImpl(Department.SOEN.toString(),"SOEN_Server.log");
				startUDPServer(soenServer,Config.getConfig("INSTANCE3_SOEN_PORT"));
			}

			return soenServer;

		} else if (serverName.equalsIgnoreCase("INSE")) {

			if (inseServer == null) {
				inseServer = new EnrollmentImpl(Department.INSE.toString(),"INSE_Server.log");
				startUDPServer(inseServer,Config.getConfig("INSTANCE3_INSE_PORT"));
			}

			return inseServer;
		}

		return null;
	}

	private static void startUDPServer(EnrollmentInterface instance, int portNo) {
		// start the department's UDP server for inter-department communication
		// the UDP server is started on a new thread
		new Thread(() -> {
			((EnrollmentImpl) instance).UDPServer(portNo);
		}).start();
	}

}
