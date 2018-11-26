package instance1.server.data;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;

public class Database {

	private static Database instance = null;	
	public static HashMap<String, SimpleEntry<Integer, Integer>> serverPorts; 
		
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
		
		serverPorts = new HashMap<>();
		serverPorts.put("COMP", new SimpleEntry<Integer, Integer>(5026, 5326));
		serverPorts.put("SOEN", new SimpleEntry<Integer, Integer>(5126, 5426));
		serverPorts.put("INSE", new SimpleEntry<Integer, Integer>(5226, 5526));
	}
	
	public static Database getInstance() {
		
		if(instance == null)
			instance = new Database();
		
		return instance;
	}
		
}