package server.instance4.pojo;

import utils.Config;

public enum Department {

	COMP(Config.getConfig("INSTANCE4_COMP_PORT")), 
	SOEN(Config.getConfig("INSTANCE4_SOEN_PORT")), 
	INSE(Config.getConfig("INSTANCE4_INSE_PORT"));
	
	int udpPort;

	private Department(int udpPort) {
		this.udpPort = udpPort;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public static boolean departmentExist(final String department) {
		for (Department dept : Department.values()) {
			if (dept.toString().equals(department.toUpperCase()))
				return true;
		}
		return false;
	}

}
