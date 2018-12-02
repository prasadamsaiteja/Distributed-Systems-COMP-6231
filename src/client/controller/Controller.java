package client.controller;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.omg.CORBA.Any;

import client.controller.data.Cache;
import utils.Logger;

public class Controller {
	
	private static String readSemester() {
		
		System.out.print("Semester: ");
		String semester = new Scanner(System.in).nextLine().toUpperCase();
		
		if(!semester.equals("FALL") && !semester.equals("WINTER") && !semester.equals("SUMMER")) {
			System.out.println("\nResult: Illegal Term Name \n");
			return null;
		}
		
		return semester;
	}

	private static String readCourseId() {
		return readCourseId("CourseId: ");
	}
	
	private static String readCourseId(String message) {
		
		System.out.print(message);
		String courseId = new Scanner(System.in).nextLine().toUpperCase();
		
		if( !(courseId.startsWith("COMP") || courseId.startsWith("SOEN") || courseId.startsWith("INSE")) ) {
			System.out.println("\nResult: Illegal Course Id \n");
			return null;
		} else if(courseId.length() != 8) {
			System.out.println("\nResult: Illegal Course Id \n");
			return null;
		}
		
		return courseId;
	}
	
	private static String readStudentId() {
		
		System.out.print("Student Id: ");
		String studentId = new Scanner(System.in).nextLine().toUpperCase();
		
		String department = studentId.substring(0, 4);
		if( !(department.equals("COMP") || department.equals("SOEN") || department.equals("INSE"))){
			System.out.println("\nResult: Invalid Student id (Department) \n");
			return null;
			
		} else if(!studentId.substring(4, 5).equals("S")) {
			System.out.println("\nResult: Invalid Student id (Client Type) \n");
			return null;
			
		} else if(studentId.length() != 9) {
			System.out.println("\nResult: Invalid Student id (Number) \n");
			return null;
		}
		
		return studentId;
	}
	
	public static void addCourse() {
		
		String semester = readSemester();
		if(semester == null) return;

		String courseId = readCourseId();
		if(courseId == null) return;
		
		if(!courseId.startsWith(Cache.getInstance().getDepartment())) {
			System.out.println("\nResult: You cant add course to other department \n");
			return;
		}
		
		System.out.print("Max Capacity: ");
		int maxCapacity = new Scanner(System.in).nextInt();
		
		if(Cache.getInstance().dcrs.addCourse(Cache.getInstance().id, courseId, semester, maxCapacity))
			Logger.addCourse_Successfull(courseId, semester, Cache.getInstance().id);
		else
			Logger.addCourse_Failed(courseId, semester, Cache.getInstance().id);
		
	}

	public static void removeCourse() {
		
		String semester = readSemester();
		if(semester == null) return;
		
		String courseId = readCourseId();
		if(courseId == null) return;
		
		if(Cache.getInstance().dcrs.removeCourse(Cache.getInstance().id, courseId, semester))
			Logger.removeCourse_Successfull(courseId, semester, Cache.getInstance().id);
		else
			Logger.removeCourse_Failed(courseId, semester, Cache.getInstance().id);
	}

	public static void listCourseAvailable() {
		
		String semester = readSemester();
		if(semester == null) return;
				
		Any any = Cache.getInstance().dcrs.listCourseAvailability(Cache.getInstance().id, semester);
		HashMap<String, Integer> courses = (HashMap<String, Integer>) any.extract_Value();
		
		Logger.listAvailableCourses(Cache.getInstance().id, semester, courses);
	}	
	
	public static void enrollCourse() {
		
		String semester = readSemester();
		if(semester == null) return;
		
		String courseId = readCourseId();
		if(courseId == null) return;
		
		String studentId = Cache.getInstance().id;
		
		if(Cache.getInstance().getClientType() == Cache.ClientType.ADVISOR){
			studentId = readStudentId();
			if(studentId == null) return;
		}
		
		Any any = Cache.getInstance().dcrs.enrolCourse(studentId, courseId, semester);
		SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) any.extract_Value();
		Logger.enrolCourse(studentId, courseId, semester, result.getKey(), result.getValue().replaceAll("_", " "));
		
	}
	
	public static void dropCourse() {
		
		String courseId = readCourseId();
		if(courseId == null) return;
		
		String studentId = Cache.getInstance().id;
		
		if(Cache.getInstance().getClientType() == Cache.ClientType.ADVISOR){
			studentId = readStudentId();
			if(studentId == null) return;
		}

		boolean result = Cache.getInstance().dcrs.dropCourse(studentId, courseId);
		Logger.dropCourse(studentId, courseId, result);		
	}

	public static void getClassSchedule() {
		
		String studentId = Cache.getInstance().id;
		
		if(Cache.getInstance().getClientType() == Cache.ClientType.ADVISOR){
			studentId = readStudentId();
			if(studentId == null) return;
		}

		Any any = Cache.getInstance().dcrs.getClassSchedule(studentId);
		HashMap<String, ArrayList<String>> result = (HashMap<String, ArrayList<String>>) any.extract_Value();
		
		Logger.getClassSchedule(studentId, result);
		
	}

	public static void swapCourse() {
		
		String studentId = Cache.getInstance().id;
		
		if(Cache.getInstance().getClientType() == Cache.ClientType.ADVISOR){
			studentId = readStudentId();
			if(studentId == null) return;
		}
		
		String oldCourseId = readCourseId("Old Course Id: ");
		if(oldCourseId == null) return;
		
		String newCourseId = readCourseId("New Course Id: ");
		if(newCourseId == null) return;
		
		Any any = Cache.getInstance().dcrs.swapCourse(studentId, newCourseId, oldCourseId);
		SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) any.extract_Value();
		
		Logger.swapCourse(studentId, newCourseId, oldCourseId, result);
		
	}

}