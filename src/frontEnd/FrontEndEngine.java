package frontEnd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.AbstractMap.SimpleEntry;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import utils.corbaInterface.IDCRSPOA;

public class FrontEndEngine extends IDCRSPOA {
	
	private ORB orb;
	private String server;

	public FrontEndEngine(String server) {
		super();
		this.server = server;
	}
	
	public void setORB(ORB orb) {
		this.orb = orb;
	}
	
	public void shutdown() {
		orb.shutdown(false);
	}

	@Override
	public boolean addCourse(String advisorId, String courseId, String semester, int capacity) {
		
		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add("addCourse")
				.add(advisorId)
				.add(courseId)
				.add(semester)
				.add(String.valueOf(capacity));
		
		String message = joiner.toString();		
		Object result = FrontEndUtitlies.sendUDPRequest(message);
				
		return (boolean) result;
	}

	@Override
	public boolean removeCourse(String advisorId, String courseId, String semester) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add("removeCourse")
				.add(advisorId)
				.add(courseId)
				.add(semester);
		
		String message = joiner.toString();		
		Object result = FrontEndUtitlies.sendUDPRequest(message);
		
		return (boolean) result;
	}

	@Override
	public Any listCourseAvailability(String advisorId, String semester) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add("listCourseAvailability")
				.add(advisorId)
				.add(semester);
		
		String message = joiner.toString();		
		HashMap<String, Integer> result = (HashMap<String, Integer>) FrontEndUtitlies.sendUDPRequest(message); 
		
		Any any = orb.create_any();
		any.insert_Value((Serializable) result);	
		return any;
	}

	@Override
	public Any enrolCourse(String studentId, String courseId, String semester) {
		
		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add("enrolCourse")
				.add(studentId)
				.add(courseId)
				.add(semester);
		
		String message = joiner.toString();		
		SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) FrontEndUtitlies.sendUDPRequest(message);
		
		Any any = orb.create_any();
		any.insert_Value((Serializable) result);	
		return any;
	}

	@Override
	public Any getClassSchedule(String studentId) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add("getClassSchedule")
				.add(studentId);
		
		String message = joiner.toString();		
		HashMap<String, ArrayList<String>> result = (HashMap<String, ArrayList<String>>) FrontEndUtitlies.sendUDPRequest(message);
		
		Any any = orb.create_any();
		any.insert_Value((Serializable) result);	
		return any;
	}

	@Override
	public boolean dropCourse(String studentId, String courseId) {
		
		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add("dropCourse")
				.add(studentId)
				.add(courseId);
		
		String message = joiner.toString();		
		Object result = FrontEndUtitlies.sendUDPRequest(message);
		
		return (boolean) result;
	}

	@Override
	public Any swapCourse(String studentId, String newCourseId, String oldCourseId) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add("swapCourse")
				.add(studentId)
				.add(newCourseId)
				.add(oldCourseId);
		
		String message = joiner.toString();		
		SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) FrontEndUtitlies.sendUDPRequest(message);
		
		Any any = orb.create_any();
		any.insert_Value((Serializable) result);	
		return any;
	}	
	
}