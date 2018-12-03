package server.instance1.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sun.xml.internal.ws.util.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;

import server.instance1.controller.socket.UDPClient;
import server.instance1.data.Database;
import server.instance1.data.Database.Terms;
import utils.Constants;
import utils.Logger;
import utils.UDPUtilities;

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

	public static byte[] deepCopyInstance1State() {
		
		HashMap<String, HashMap<String, HashMap<String, Object>>> deepCopyDatabase = new HashMap<>();	
		HashMap<Terms, HashMap<String, HashMap<String, String>>> database = Database.getInstance().courses;
		
		for (Entry<Terms, HashMap<String, HashMap<String, String>>> semester : database.entrySet()) {
			
			HashMap<String, HashMap<String, String>> courses = semester.getValue();
			HashMap<String, HashMap<String, Object>> deepCopyCourses = new HashMap<>();
			
			for (Entry<String, HashMap<String, String>> course : courses.entrySet()) {
				
				HashMap<String, String> courseProperties = course.getValue();
				HashMap<String, Object> deepCopyCourseProperties = new HashMap<>();
					
				for (Entry<String, String> courseProperty : courseProperties.entrySet()) {
				
					if(courseProperty.getKey().equals("max_capacity"))
						deepCopyCourseProperties.put(Constants.CAPACITY, Integer.parseInt(courseProperty.getValue()));
					
					else if(courseProperty.getKey().equals("capacity"))
						deepCopyCourseProperties.put(Constants.STUDENTS_ENROLLED, Integer.parseInt(courseProperty.getValue()));
					
					else if(courseProperty.getKey().equals("enrolled_students")) {
						String[] students = courseProperty.getValue().split("&");
						HashSet<String> set = new HashSet<>();
						for (String student : students) 
							set.add(student);
						
						deepCopyCourseProperties.put(Constants.STUDENT_IDS, set); //Course properties
					}
				}
				
				deepCopyCourses.put(new String(course.getKey()), deepCopyCourseProperties); // Course
				
			}
			
			deepCopyDatabase.put(new String(semester.getKey().toString()), deepCopyCourses); //Term
			
		}
		
		
		System.out.println("Getting state from Instance 1 : "+deepCopyDatabase);
		
		return UDPUtilities.objectToByteArray(deepCopyDatabase);
		
	}

	public static void setState(Object object) {
	
		HashMap<String, HashMap<String, HashMap<String, Object>>> data = (HashMap<String, HashMap<String, HashMap<String, Object>>>) object;
		HashMap<Terms, HashMap<String, HashMap<String, String>>> database = new HashMap<>();
		
		for (Entry<String, HashMap<String, HashMap<String, Object>>> semester : data.entrySet()) {
			
			HashMap<String, HashMap<String, Object>> courses = semester.getValue();
			HashMap<String, HashMap<String, String>> databaseCourses = new HashMap<>();
			
			for (Entry<String, HashMap<String, Object>> course : courses.entrySet()) {
				
				HashMap<String, Object> courseProperties = course.getValue();
				HashMap<String, String> databaseCourseProperties = new HashMap<>();
					
				for (Entry<String, Object> courseProperty : courseProperties.entrySet()) {
				
					if(courseProperty.getKey().equals(Constants.CAPACITY))
						databaseCourseProperties.put("max_capacity", String.valueOf(courseProperty.getValue()));
					
					else if(courseProperty.getKey().equals(Constants.STUDENTS_ENROLLED))
						databaseCourseProperties.put("capacity", String.valueOf(courseProperty.getValue()));
					
					else if(courseProperty.getKey().equals("enrolled_students")) {
						HashSet<String> set = (HashSet<String>) courseProperty.getValue();
						String students = String.join("&", set);
						
						databaseCourseProperties.put(Constants.STUDENT_IDS, students); //Course properties
					}
				}
				
				databaseCourses.put(new String(course.getKey()), databaseCourseProperties); // Course
				
			}
			
			database.put(Database.Terms.valueOf(semester.getKey()), databaseCourses); //Term
			
		}
		
		Database.setDatabase(database);
				
	}
	
}