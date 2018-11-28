package server.instance2.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
	
	Logger logger = Logger.getLogger("LogManager");
	
	public LogManager(String fileName) throws IOException{
		FileHandler fh = new FileHandler(fileName,true);
		SimpleFormatter sf = new SimpleFormatter();
		fh.setFormatter(sf);
		logger.addHandler(fh);

	}
	
	public void writeLog(String line){
		logger.info(line);
	}
	
	

}
