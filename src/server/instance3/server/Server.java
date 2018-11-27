/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3.server;

import java.io.File;
import java.io.IOException;

import generic.Config;
import server.instance3.logging.MyLogger;
import server.instance3.remoteObject.EnrollmentImpl;
import server.instance3.remoteObject.EnrollmentInterface;
import server.instance3.util.Constants;
import server.instance3.util.Department;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target=
 *      "_blank">Profile</a>
 */
public class Server {

	private static EnrollmentInterface compServer;
	private static EnrollmentInterface soenServer;
	private static EnrollmentInterface inseServer;

	public static EnrollmentInterface getInstance(String serverName) throws IOException {

		if (serverName.equalsIgnoreCase("COMP")) {

			if (compServer == null) {
				setupLogging("COMP_Server.log");
				compServer = new EnrollmentImpl(Department.COMP.toString());
				startUDPServer(compServer, Config.getConfig("INSTANCE3_COMP_PORT"));
			}
			return compServer;

		} else if (serverName.equalsIgnoreCase("SOEN")) {

			if (soenServer == null) {
				setupLogging("SOEN_Server.log");
				soenServer = new EnrollmentImpl(Department.SOEN.toString());
				startUDPServer(soenServer,Config.getConfig("INSTANCE3_SOEN_PORT"));
			}

			return soenServer;

		} else if (serverName.equalsIgnoreCase("INSE")) {

			if (inseServer == null) {
				setupLogging("INSE_Server.log");
				inseServer = new EnrollmentImpl(Department.INSE.toString());
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

	/**
	 * Logging setup for COMP server
	 * 
	 * @throws IOException
	 */
	private static void setupLogging(String fileName) throws IOException {
		File files = new File(Constants.SERVER_LOG_DIRECTORY);
		if (!files.exists())
			files.mkdirs();
		files = new File(Constants.SERVER_LOG_DIRECTORY + fileName);
		if (!files.exists())
			files.createNewFile();
		MyLogger.setup(files.getAbsolutePath());
	}

}
