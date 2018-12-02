package server.instance1.controller.corba;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

import server.instance1.controller.Helper;
import server.instance1.controller.socket.UDPClient;
import server.instance1.data.Database;
import utils.Logger;

public class DCRS {
		
	public static synchronized boolean addCourse(String adivsorId, String courseId, String semester, int capacity) {
		
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

	public static synchronized boolean removeCourse(String adivsorId, String courseId, String semester) {
		
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

	public static synchronized HashMap<String, Integer> listCourseAvailability(String advisorId, String semester) {
		
		HashMap<String, Integer> result = Helper.listCourseAvailability(semester);
		
		result.putAll(UDPClient.getListCourseAvailability(semester));
		Logger.listAvailableCourses(advisorId, semester, result);
		
		return result;
	}

	public static synchronized SimpleEntry<Boolean, String> enrolCourse(String studentID, String courseId, String semester) {

		SimpleEntry<Boolean, String> result;
		
		if(courseId.startsWith(Database.getInstance().department))
			result = Helper.enrolCourse(studentID, courseId, semester, true);
		else
			result = UDPClient.enrolCourse(studentID, courseId, semester);
				
		return result;
	}

	public static synchronized HashMap<String, ArrayList<String>> getClassSchedule(String studentId) {

		HashMap<String, ArrayList<String>> result = UDPClient.getClassSchedule(studentId);
		Logger.getClassSchedule(studentId, result);
		
		return result;		
	}

	public static synchronized boolean dropCourse(String studentId, String courseId) {
		
		if(courseId.startsWith(Database.getInstance().department))
			return Helper.dropCourse(studentId, courseId);
		else
			return UDPClient.dropCourse(studentId, courseId);
		
	}

	public static synchronized SimpleEntry<Boolean, String> swapCourse(String studentId, String newCourseId, String oldCourseId) {
		
		SimpleEntry<Boolean, String> result = Helper.swapCourse(studentId, newCourseId, oldCourseId);
		Logger.swapCourse(studentId, newCourseId, oldCourseId, result);
		
		return result;
	}

}