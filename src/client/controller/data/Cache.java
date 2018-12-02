package client.controller.data;

import utils.corbaInterface.IDCRS;

public class Cache {

	private static Cache instance = null;
	
	public IDCRS dcrs;
	public String id;
	public enum ClientType {
		STUDENT, ADVISOR
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