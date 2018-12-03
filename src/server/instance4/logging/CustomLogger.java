package server.instance4.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomLogger {
	static FileHandler fh;

	public static void setUpLogger(final String fileName) throws IOException {
		Logger logger = Logger.getLogger(fileName);
		logger.setUseParentHandlers(false);

		logger.setLevel(Level.INFO);
		fh = new FileHandler(fileName, true);
		fh.setFormatter(new CustomFormatter());
		logger.addHandler(fh);
	}

}
