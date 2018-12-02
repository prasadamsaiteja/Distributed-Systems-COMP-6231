/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package replicaManager;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import server.instance3.logging.MyLogger;
import utils.Config;
import utils.Constants;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target="_blank">Profile</a>
 */
public class ReplicaManager {
	
	public static void main(String[] args) {
		
		try {
			setupLogging("RM.log");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IntStream.rangeClosed(1, 4).forEach(i -> {
			new Thread(new ReplicaManagerEngine(i,Config.getConfig("RM"+i+"_PORT"))).start();;
		});
		
		System.out.println("Replica Manager initated");
	}
	
	private static void setupLogging(String fileName) throws IOException {
		File files = new File(Constants.RM_LOG_DIRECTORY);
		if (!files.exists())
			files.mkdirs();
		files = new File(Constants.RM_LOG_DIRECTORY + fileName);
		if (!files.exists())
			files.createNewFile();
		MyLogger.setup(files.getAbsolutePath());
	}
	

}
