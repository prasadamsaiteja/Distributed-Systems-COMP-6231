package server.instance2.Interface;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Addition {
		
	public boolean addCourse( String advisorId, String courseID, String semester, int capacity) ;
	
	public boolean removeCourse( String advisorId, String courseID, String semester) ;
	
	public SimpleEntry<Boolean,String> enrolCourse(String studentID, String courseID, String semester);
	
	public boolean dropCourse( String studentID, String courseID) ;
	
	public HashMap<String,Integer> listCourseAvailability(String advisorId, String semester) ;
	
	public HashMap<String,ArrayList<String>> getClassSchedule( String studentId) ;
	
	public SimpleEntry<Boolean,String> swapCourse( String studentID, String newCourseID, String oldCourseID);
	
}
