/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom logging file
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
 *
 */
public class MyLogger {

	/** fileHandler for logging to file */
	static private FileHandler fileHandler;

	public static void setup(final String logFile) throws IOException {
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		logger.setUseParentHandlers(false); // don't log the console output to file
		logger.setLevel(Level.INFO);
		fileHandler = new FileHandler(logFile, true);
		fileHandler.setFormatter(new MyFormatter());
		logger.addHandler(fileHandler);
	}
}
