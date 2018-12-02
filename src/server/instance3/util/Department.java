/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3.util;

import utils.Config;

/**
 * Department Enumeration
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
 *
 */
public enum Department {

	COMP(Config.getConfig("INSTANCE3_COMP_PORT")), 
	SOEN(Config.getConfig("INSTANCE3_SOEN_PORT")),
	INSE(Config.getConfig("INSTANCE3_INSE_PORT"));
	
	int udpPort;

	private Department(int udpPort) {
		this.udpPort = udpPort;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public static boolean departmentExist(String dept) {
		for (Department d : Department.values()) {
			if (d.toString().equals(dept.toUpperCase()))
				return true;
		}
		return false;
	}
}
