/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3.remoteObject;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface contract for the Distributed Course Registration System
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
 *
 */

public interface EnrollmentInterface extends Remote {

	/* Advisor Operations */

	boolean addCourse(String advisorId, String courseId, String semester, int capacity) throws RemoteException;

	boolean removeCourse(String advisorId, String courseId, String semester) throws RemoteException;

	HashMap<String, Integer> listCourseAvailability(String advisorId, String semester) throws RemoteException;

	/* Student Operations */

	SimpleEntry<Boolean, String> enrolCourse(String studentId, String courseId, String semester) throws RemoteException;

	HashMap<String, ArrayList<String>> getClassSchedule(String studentId) throws RemoteException;

	boolean dropCourse(String studentId, String courseId) throws RemoteException;
	
	SimpleEntry<Boolean, String> swapCourse(String studentId,String newCourseId,String oldCourseId) throws RemoteException;
	
	byte[] getInternalState();

	void setState(HashMap<String, HashMap<String, HashMap<String, Object>>> data);

}
