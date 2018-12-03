package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import client.controller.data.Cache;
import server.instance1.data.Database;

public class Logger {
	
	public static boolean isServer = false;

	public static void log(String logStatement) {
		
		String time = new SimpleDateFormat("[HH:mm:ss dd/MM/yyyy]").format(new java.util.Date()) + " "; 
		
		if(!isServer) System.out.print("\nResult: ");
		else {
			System.out.print(time);
			System.out.print("[" + Database.getInstance().department + "] ");
		}
		
		System.out.println(logStatement);
		
		if(!isServer)
			System.out.println("");
						
		writeLogToFile(time + logStatement);
	}
	
	private static void writeLogToFile(String logstatement){
		
		logstatement += "\n";
		
		try{
			if(isServer){
				File logFile = new File("./Logs/Instance1/ServerLogs/" + Database.getInstance().department + ".txt");
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
				
				FileOutputStream logFileOutputStream = new FileOutputStream(logFile, true);
				logFileOutputStream.write(logstatement.getBytes());
				logFileOutputStream.close();
			}
			
			else {
				
				String folder = "../Logs/Instance1/ClientLogs/";
				
				if(Cache.getInstance().getClientType() == Cache.ClientType.ADVISOR) folder += "Advisor/";
				else folder += "Student/";
				
				File logFile = new File(folder + Cache.getInstance().id + ".txt");
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
				
				FileOutputStream logFileOutputStream = new FileOutputStream(logFile, true);
				logFileOutputStream.write(logstatement.getBytes());
				logFileOutputStream.close();
			}
			
		} catch (IOException ignored){}
		
	}
	
	public static void addCourse_Successfull(String courseId, String semester, String advisorId) {
		log("[Add Course] [Successfull] " + courseId + " successfully added to " + semester + " semester, by " + advisorId);
	}
	
	public static void addCourse_Failed(String courseId, String semester, String advisorId) {
		log("[Add Course] [Failed] " +courseId + " failed to add to " + semester + " semester, by " + advisorId + " as it is already exists");
	}

	public static void removeCourse_Failed(String courseId, String semester, String adivsorId) {
		log("[Remove Course] [Failed] " + courseId + " failed to remove from " + semester + " semester, by " + adivsorId + " as it is doesn't exists");
	}

	public static void removeCourse_Successfull(String courseId, String semester, String adivsorId) {
		log("[Remove Course] [Successfull] " + courseId + " successfully removed from " + semester + " semester, by " + adivsorId);
	}
	
	public static void listAvailableCourses(String advisorId, String semester, HashMap<String, Integer> result){
		
		String temp = "[List Available Courses] [Successfull] " + advisorId + " has requested course availability for " + semester + " semester \n";
		
		for (Entry<String, Integer> course : result.entrySet())
			temp += course.getKey() + " => " + course.getValue() + ", ";
		
		log(temp);
	}
	
	public static void enrolCourse(String studentId, String courseId, String semester, boolean status, String message) {
		
		if(status)
			log("[Enrol Course] [Successfull] " + studentId + " has enrolled into course " + courseId + " for " + semester + " semester \n");
		else
			log("[Enrol Course] [Failed] " + studentId + " has tried to enrolled into course " + courseId + " for " + semester + " semester failed because of " + message + " \n");
				
	}
	
	public static void dropCourse(String studentId, String courseId, boolean status) {
		
		if(status)
			log("[Drop Course] [Successfull] " + studentId + " has been successfully dropped from course " + courseId + " \n");
		else
			log("[Drop Course] [Failed] " + studentId + " has tried to drop from course " + courseId + " but failed because he isn't enrolled it in any semester \n");

	}
	
	public static void getClassSchedule(String studentId, HashMap<String, ArrayList<String>> result) {
		
		String temp = "[Class Schedule] [Successfull] " + studentId + " has requested class schedule \n";
		
		for (Entry<String, ArrayList<String>> entry : result.entrySet()) {
			
			temp += entry.getKey() + ": ";
			
			if(entry.getValue().size() > 0) {
				for (String course: entry.getValue())
					temp += course + ", ";
			} else
				temp += "None";
			
			temp += "\n";
		}
		
		log(temp);
		
	}

	public static void swapCourse(String studentId, String newCourseId, String oldCourseId, SimpleEntry<Boolean, String> result) {
				
		if(result.getKey())
			log("[Swap Course] [Successfull] " + studentId + " has successfully swapped course from " + oldCourseId + " to " + newCourseId);
		else 
			log("[Swap Course] [Failed] " + studentId + " has requested to swap course from " + oldCourseId + " to " + newCourseId + " but failed because " + result.getValue());

	}
	
}