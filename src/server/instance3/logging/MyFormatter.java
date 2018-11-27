/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Custom log formatter
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
 *
 */
public class MyFormatter extends Formatter {

	private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(LogRecord record) {

		StringBuilder builder = new StringBuilder();
		builder.append(df.format(new Date(record.getMillis()))).append(" - ");
//      builder.append("[").append(record.getSourceClassName()).append(".");
//      builder.append(record.getSourceMethodName()).append("] - ");
		builder.append("[").append(record.getLevel()).append("] - ");
		builder.append(formatMessage(record));
		builder.append("\n");
		return builder.toString();
	}

}
