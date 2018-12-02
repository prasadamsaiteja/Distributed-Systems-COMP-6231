package server.instance1.data;

import java.util.HashMap;

import utils.Config;

public class Database {

	private static Database compInstance = null;
	private static Database soenInstance = null;
	private static Database inseInstance = null;
	
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
		
		if(Thread.currentThread().getName().equals("COMP"))
			return compInstance;
		
		else if(Thread.currentThread().getName().equals("SOEN"))
			return soenInstance;
		
		else if(Thread.currentThread().getName().equals("INSE"))
			return inseInstance;
		
		return null;
			
	}
	
	public static Database getInstance(String serverName) {
		
		if(serverName.equals("COMP")) {
		
			if(compInstance == null) {
				compInstance = new Database();
				compInstance.department = "COMP";
			}
			
			return compInstance;
			
		} else if(serverName.equals("SOEN")) {
			
			if(soenInstance == null){
				soenInstance = new Database();
				soenInstance.department = "SOEN";
			}
			
			return soenInstance;
			
		} else if(serverName.equals("INSE")) {
			
			if(inseInstance == null){
				inseInstance = new Database();
				inseInstance.department = "INSE";
			}
			
			return inseInstance;
			
		}

		return null;
	}

	public static void setDatabase(HashMap<Terms, HashMap<String, HashMap<String, String>>> database) {
		
		if(Thread.currentThread().getName().equals("COMP"))
			compInstance.courses = database;
		
		else if(Thread.currentThread().getName().equals("SOEN"))
			soenInstance.courses = database;
		
		else if(Thread.currentThread().getName().equals("INSE"))
			inseInstance.courses = database;
		
	}
		
}