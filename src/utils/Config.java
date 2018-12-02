package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

	private static Properties properties;

	public static int getConfig(String variable) {

		int value = -1;

		try {

			if (properties == null) {
				properties = new Properties();
				properties.load(new FileInputStream("app.config"));
			}

			value = Integer.parseInt(properties.getProperty(variable));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return value;
	}

	public static String getStringConfig(String variable){

		try {
			if (properties == null) {
				properties = new Properties();
				properties.load(new FileInputStream("app.config"));
			}
		}catch(IOException e) {
			
		}

		return properties.getProperty(variable);
	}

}