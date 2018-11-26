package client.controller.data;

import java.util.HashMap;

import generic.corbaInterface.IDCRS;

public class Cache {

	private static Cache instance = null;
	public static HashMap<String, Integer> serverPorts;
	
	public IDCRS dcrs;
	public String id;
	public enum ClientType {
		STUDENT, ADVISOR
	}
	
	private Cache() {
		serverPorts = new HashMap<>();
		serverPorts.put("COMP", 5026);
		serverPorts.put("SOEN", 5126);
		serverPorts.put("INSE", 5226);
	}
	
	public static Cache getInstance() {
		
		if(instance == null)
			instance = new Cache();
		
		return instance;
	}
	
	public String getDepartment() {
		return id.substring(0, 4);
	}

	public ClientType getClientType() {
		
		if(id.substring(4, 5).equals("A")) 
			return ClientType.ADVISOR;		
		else if(id.substring(4, 5).equals("S")) 
			return ClientType.STUDENT;
		
		return null;
	}

	public boolean checkValidId() {
		
		String department = this.getDepartment();
		
		if( !(department.equals("COMP") || department.equals("SOEN") || department.equals("INSE"))){
			System.out.println("Invalid Student id (Department)");
			return false;
			
		} else if(this.getClientType() == null) {
			System.out.println("Invalid Student id (Client Type)");
			return false;
			
		} else if(id.length() != 9) {
			System.out.println("Invalid Student id (Number)");
			return false;
		}
			
		return true;
	}
	
}