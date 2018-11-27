/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3.util;

/**
 * Department Enumeration
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
 *
 */
public enum Department {

	COMP(4444, 8080), SOEN(5555, 8081), INSE(6666, 8082);
	int udpPort, webServicePort;

	private Department(int udpPort, int webServicePort) {
		this.udpPort = udpPort;
		this.webServicePort = webServicePort;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public int getWebServicePort() {
		return webServicePort;
	}

	public static boolean departmentExist(String dept) {
		for (Department d : Department.values()) {
			if (d.toString().equals(dept.toUpperCase()))
				return true;
		}
		return false;
	}
}
