package server.instance1.data;

import java.util.HashMap;

import generic.Config;

public class Database {

	private static Database instance = null;	
	public static HashMap<String, Integer> serverPorts; 
		
	public enum Terms {
		FALL, WINTER, SUMMER
	}
	
	//Terms -> Courses -> Course Details
	public HashMap<Terms, HashMap<String, HashMap<String, String>>> courses;
	public String department;
	
	private Database() {
		courses = new HashMap<>();
		courses.put(Terms.WINTER, new HashMap<>());
		courses.put(Terms.FALL, new HashMap<>());
		courses.put(Terms.SUMMER, new HashMap<>());
		
		try {
			
			serverPorts = new HashMap<>();
			serverPorts.put("COMP", Config.getConfig("INSTANCE1_COMP_PORT"));
			serverPorts.put("SOEN", Config.getConfig("INSTANCE1_SOEN_PORT"));
			serverPorts.put("INSE", Config.getConfig("INSTANCE1_INSE_PORT"));
			
		} catch(Exception ex){
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	public static Database getInstance() {
		
		if(instance == null)
			instance = new Database();
		
		return instance;
	}
		
}