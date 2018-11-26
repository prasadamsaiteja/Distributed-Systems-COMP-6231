package generic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {
	
	private static Properties properties;
	
	public static int getConfig(String variable) throws FileNotFoundException, IOException {
		
		if(properties == null){
			 properties = new Properties();
			 properties.load(new FileInputStream("app.config"));
		}
		
		return Integer.parseInt(properties.getProperty(variable));
	}
	
}