package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {

	private static Properties properties;
	private static Map<String, String> reverseMap;

	static {
		try {
			properties = new Properties();
			reverseMap = new HashMap<>();
			properties.load(new FileInputStream("app.config"));
			for(String key : properties.stringPropertyNames()) {
				reverseMap.put(properties.getProperty(key), key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

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

	public static String getStringConfig(String variable) {

		try {
			if (properties == null) {
				properties = new Properties();
				properties.load(new FileInputStream("app.config"));
			}
		} catch (IOException e) {

		}

		return properties.getProperty(variable);
	}
	
	
	
	public static String getReverseMaping(int portNo) {
		return reverseMap.get(Integer.valueOf(portNo).toString());
	}

}