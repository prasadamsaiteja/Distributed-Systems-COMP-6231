package instance1.server.controller.rmi;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import instance1.corbaInterface.IDCRSPOA;
import instance1.server.controller.Helper;
import instance1.server.controller.socket.UDPClient;
import instance1.server.data.Database;
import instance1.logging.Logger;

public class DCRS extends IDCRSPOA {
	
	private ORB orb;	

	public DCRS() {
		super();
	}
	
	public void setORB(ORB orb) {
		this.orb = orb;
	}
	
	public void shutdown() {
		orb.shutdown(false);
	}

	@Override
	public synchronized boolean addCourse(String adivsorId, String courseId, String semester, int capacity) {
		
		Database.Terms sem = Database.Terms.valueOf(semester.toUpperCase());
		Database databaseInstance = Database.getInstance();
		
		if(databaseInstance.courses.get(sem).get(courseId) != null) {
			Logger.addCourse_Failed(courseId, semester, adivsorId);
			return false;
		}
					
		HashMap<String, String> courseDetails = new HashMap<>();
		courseDetails.put("max_capacity", String.valueOf(capacity));
		courseDetails.put("capacity", Integer.toString(0));
		courseDetails.put("enrolled_students", "");
		
		databaseInstance.courses.get(sem).put(courseId, courseDetails);		
		Logger.addCourse_Successfull(courseId, semester, adivsorId);
		
		return true;
	}

	@Override
	public synchronized boolean removeCourse(String adivsorId, String courseId, String semester) {
		
		Database.Terms sem = Database.Terms.valueOf(semester.toUpperCase());
		Database databaseInstance = Database.getInstance();
		
		if(databaseInstance.courses.get(sem).get(courseId) == null) {
			Logger.removeCourse_Failed(courseId, semester, adivsorId);
			return false;
		}
		
		databaseInstance.courses.get(sem).remove(courseId);
		Logger.removeCourse_Successfull(courseId, semester, adivsorId);
		return true;
		
	}

	@Override
	public synchronized Any listCourseAvailability(String advisorId, String semester) {
		
		HashMap<String, Integer> result = Helper.listCourseAvailability(semester);
		
		result.putAll(UDPClient.getListCourseAvailability(semester));
		Logger.listAvailableCourses(advisorId, semester, result);
		
		Any any = orb.create_any(); 		
		any.insert_Value((Serializable) result);
		return any;
	}

	@Override
	public synchronized Any enrolCourse(String studentID, String courseId, String semester) {

		SimpleEntry<Boolean, String> result;
		
		if(courseId.startsWith(Database.getInstance().department))
			result = Helper.enrolCourse(studentID, courseId, semester, true);
		else
			result = UDPClient.enrolCourse(studentID, courseId, semester);
		
		Any any = orb.create_any();
		any.insert_Value((Serializable) result);		
		return any;
	}

	@Override
	public synchronized Any getClassSchedule(String studentId) {

		HashMap<String, ArrayList<String>> result = UDPClient.getClassSchedule(studentId);
		Logger.getClassSchedule(studentId, result);

		Any any = orb.create_any();
		any.insert_Value((Serializable) result);		
		return any;		
	}

	@Override
	public synchronized boolean dropCourse(String studentId, String courseId) {
		
		if(courseId.startsWith(Database.getInstance().department))
			return Helper.dropCourse(studentId, courseId);
		else
			return UDPClient.dropCourse(studentId, courseId);
		
	}

	
	@Override
	public synchronized Any swapCourse(String studentId, String newCourseId, String oldCourseId) {
		
		SimpleEntry<Boolean, String> result = Helper.swapCourse(studentId, newCourseId, oldCourseId);
		Logger.swapCourse(studentId, newCourseId, oldCourseId, result);
		
		Any any = orb.create_any(); 		
		any.insert_Value((Serializable) result);
		return any;
	}

}