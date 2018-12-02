package server.instance1.controller;

import java.util.HashMap;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;

import server.instance1.controller.socket.UDPClient;
import server.instance1.data.Database;
import utils.Logger;

public class Helper {

	public synchronized static HashMap<String, Integer> listCourseAvailability(String semester) {
		
		Database.Terms sem = Database.Terms.valueOf(semester.toUpperCase());
		Database databaseInstance = Database.getInstance();
		
		Set<Entry<String, HashMap<String, String>>> availableCourses = databaseInstance.courses.get(sem).entrySet();
		HashMap<String, Integer> result = new HashMap<>();
		
		for (Entry<String, HashMap<String, String>> entry : availableCourses) {
			
			HashMap<String, String> courseDetails = entry.getValue();
			
			int placesOccupied = 0;
			int maxCapacity = Integer.valueOf(courseDetails.get("max_capacity"));
			
			if(courseDetails.containsKey("capacity"))
				placesOccupied = Integer.parseInt(courseDetails.get("capacity"));
			
			result.put(entry.getKey(), maxCapacity - placesOccupied);
		}
		
		return result;
	}

	public synchronized static SimpleEntry<Boolean, String> enrolCourse(String studentID, String courseId, String semester, boolean log) {
		
		Database.Terms sem = Database.Terms.valueOf(semester.toUpperCase());
		Database databaseInstance = Database.getInstance();
		
		if(databaseInstance.courses.get(sem).get(courseId) != null) {
			
			HashMap<String, String> courseDetails = databaseInstance.courses.get(sem).get(courseId);
			if(courseDetails.get("max_capacity").equals(courseDetails.get("capacity"))) {
				if(log) Logger.enrolCourse(studentID, courseId, semester, false, "course full");
				return new SimpleEntry<>(false, "course_full");
			}
			
			else{

				if(courseDetails.get("enrolled_students").contains(studentID)) {
					if(log) Logger.enrolCourse(studentID, courseId, semester, false, "already enrolled");
					return new SimpleEntry<>(false, "already_enrolled");
					
				} else {

					SimpleEntry<Integer, Integer> enrollmentResult = UDPClient.getCountOfEnrolledCourses(studentID, semester);				
					
					if(enrollmentResult.getKey() == 3){
						if(log) Logger.enrolCourse(studentID, courseId, semester, false, "already enrolled in 3 courses for this semester");
						return new SimpleEntry<>(false, "already_enrolled_in_3_courses_for_this_semester");						
					
					} else if(enrollmentResult.getValue() == 2 && !courseId.contains(studentID.substring(0, 3))){
						if(log) Logger.enrolCourse(studentID, courseId, semester, false, "already enrolled in 2 off department courses");
						return new SimpleEntry<>(false, "already_enrolled_in_2_off_department_courses");						
						
					} else if(checkIfAlreadyRegisteredInDifferentSemester(studentID, courseId, semester)){
						if(log) Logger.enrolCourse(studentID, courseId, semester, false, "already enrolled in different semester");
						return new SimpleEntry<>(false, "already_enrolled_in_different_semester");
					}
				
					int currentCapacity = 0;
					
					if(courseDetails.containsKey("capacity"))
						currentCapacity = Integer.parseInt(courseDetails.get("capacity"));
	
					currentCapacity++;
					courseDetails.put("capacity", Integer.toString(currentCapacity));
					
					if(courseDetails.get("enrolled_students").equals(""))
						courseDetails.put("enrolled_students", studentID);				
					else
						courseDetails.put("enrolled_students", courseDetails.get("enrolled_students") + "&" + studentID);
					
					Database.getInstance().courses.get(sem).put(courseId, courseDetails);
				
					if(log) Logger.enrolCourse(studentID, courseId, semester, true, null);
					return new SimpleEntry<>(true, "");
				}
			}
		}
		
		if(log) Logger.enrolCourse(studentID, courseId, semester, false, "course doesnt exist");
		return new SimpleEntry<>(false, "course_doesnt_exist");

	}

	public synchronized static boolean dropCourse(String studentId, String courseId) {
		
		Database databaseInstance = Database.getInstance();
		
		for (Database.Terms sem : Database.Terms.values()) {
			if(databaseInstance.courses.get(sem).get(courseId) != null){
		
				HashMap<String, String> courseDetails = databaseInstance.courses.get(sem).get(courseId);
				if(courseDetails.get("enrolled_students").contains(studentId)){
					
					String currentEnrollementList = courseDetails.get("enrolled_students");
					currentEnrollementList = currentEnrollementList.replaceAll(studentId, "");
					
					int currentCapacity = Integer.parseInt(courseDetails.get("capacity"));
					currentCapacity--;
					
					courseDetails.put("capacity", Integer.toString(currentCapacity));					
					courseDetails.put("enrolled_students", currentEnrollementList);
					Database.getInstance().courses.get(sem).put(courseId, courseDetails);
					
					Logger.dropCourse(studentId, courseId, true);
					return true;
				}				
				
			}
		}
		
		Logger.dropCourse(studentId, courseId, false);
		return false;
	}

	public synchronized static SimpleEntry<Boolean, String> swapCourse(String studentId, String newCourseId, String oldCourseId) {

		boolean oldCourseAlreadyEnrolled = false, newCourseAlreadyEnrolled = false;
		String semester = null;
		
		//All Class Schedule Of Student
		HashMap<String, ArrayList<String>> studentEnrolledCourses = UDPClient.getClassSchedule(studentId);
		for (Entry<String, ArrayList<String>> entrySet : studentEnrolledCourses.entrySet()) {
			
			if(entrySet.getValue().contains(oldCourseId)) {
				oldCourseAlreadyEnrolled = true;
				semester = entrySet.getKey();
			}
			if(entrySet.getValue().contains(newCourseId))
				newCourseAlreadyEnrolled = true;
		}
		
		if(!oldCourseAlreadyEnrolled)
			return new SimpleEntry<Boolean, String>(false, "Didn't enroll in old course");
		
		if(newCourseAlreadyEnrolled)
			return new SimpleEntry<Boolean, String>(false, "Previously Enrolled in the newly required course");
		
		HashMap<String, Integer> listCourseAvailabilityResult = Helper.listCourseAvailability(semester);
		if(!listCourseAvailabilityResult.containsKey(newCourseId))
			return new SimpleEntry<Boolean, String>(false, "New required course doesn't exists");
		
		if(listCourseAvailabilityResult.get(newCourseId) == 0)
			return new SimpleEntry<Boolean, String>(false, "New required course is full");
		
		//Drop Course
		if(oldCourseId.startsWith(Database.getInstance().department))
			Helper.dropCourse(studentId, oldCourseId);
		else
			UDPClient.dropCourse(studentId, oldCourseId);
		
		//Enroll Course
		SimpleEntry<Boolean, String> enrollCourseResult;
		if(newCourseId.startsWith(Database.getInstance().department))
			enrollCourseResult = Helper.enrolCourse(studentId, newCourseId, semester, false);
		else
			enrollCourseResult = UDPClient.enrolCourse(studentId, newCourseId, semester);
		
		if(enrollCourseResult.getKey() == false) {
			
			if(oldCourseId.startsWith(Database.getInstance().department))
				Helper.enrolCourse(studentId, oldCourseId, semester, false);
			else
				UDPClient.enrolCourse(studentId, oldCourseId, semester);
			
			return new SimpleEntry<Boolean, String>(false, enrollCourseResult.getValue());
		}
		
		return new SimpleEntry<Boolean, String>(true, "yay");
	}

	public static HashMap<String, ArrayList<String>> getClassSchedule(String studentId) {
		
		Database databaseInstance = Database.getInstance();
		HashMap<String, ArrayList<String>> result = new HashMap<>();
		
		for (Database.Terms sem : Database.Terms.values()) {
			
			ArrayList<String> semesterEnrolledCourses = new ArrayList<>();
			
			Set<Entry<String, HashMap<String, String>>> availableCourses = databaseInstance.courses.get(sem).entrySet();			
			for (Entry<String, HashMap<String, String>> entry : availableCourses) {				
				if(entry.getValue().get("enrolled_students").contains(studentId))
					semesterEnrolledCourses.add(entry.getKey());				
			}
			
			result.put(sem.name(), semesterEnrolledCourses);
		}
		
		return result;
	}

	public static boolean checkIfAlreadyRegisteredInDifferentSemester(String studentId, String courseId, String semester){
		
		Database databaseInstance = Database.getInstance();
		
		for (Database.Terms sem : Database.Terms.values()) {
			
			if(sem.name().equals(semester))
				continue;
			
			if(databaseInstance.courses.get(sem).get(courseId) != null){
		
				HashMap<String, String> courseDetails = databaseInstance.courses.get(sem).get(courseId);
				if(courseDetails.get("enrolled_students").contains(studentId))
					return true;
			}
			
		}
		
		return false;
	}
	
	public static SimpleEntry<Integer, Integer> getCountOfEnrolledCourses(String studentId, String semester){
				
		SimpleEntry<Integer, Integer> result = new SimpleEntry<Integer, Integer>(0, 0);
		
		for (Database.Terms currentSemester : Database.Terms.values()) {									
			for(Entry<String, HashMap<String, String>> course: Database.getInstance().courses.get(currentSemester).entrySet()){							
				
				if(course.getValue().get("enrolled_students").contains(studentId)){
					
					if(currentSemester.name().equals(semester))
						result = new SimpleEntry<Integer, Integer>(result.getKey() + 1, result.getValue());	
				
					if(!studentId.contains(Database.getInstance().department))
						result = new SimpleEntry<Integer, Integer>(result.getKey(), result.getValue() + 1);
				}
				
			}			
		}
		
		return result;		
	}
	
}