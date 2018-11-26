package frontEnd;

import java.util.StringJoiner;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import generic.corbaInterface.IDCRSPOA;

public class FrontEnd extends IDCRSPOA {
	
	private ORB orb;
	private String server;

	public FrontEnd(String server) {
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
				.add(advisorId)
				.add(courseId)
				.add(semester)
				.add(String.valueOf(capacity));
		
		String message = joiner.toString();		
		FrontEndUtitlies.sendUDPRequest(message);
		
		return false;
	}

	@Override
	public boolean removeCourse(String advisorId, String courseId, String semester) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add(advisorId)
				.add(courseId)
				.add(semester);
		
		String message = joiner.toString();		
		FrontEndUtitlies.sendUDPRequest(message);
		
		return false;
	}

	@Override
	public Any listCourseAvailability(String advisorId, String semester) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add(advisorId)
				.add(semester);
		
		String message = joiner.toString();		
		FrontEndUtitlies.sendUDPRequest(message);
		
		return null;
	}

	@Override
	public Any enrolCourse(String studentId, String courseId, String semester) {
		
		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add(studentId)
				.add(courseId)
				.add(semester);
		
		String message = joiner.toString();		
		FrontEndUtitlies.sendUDPRequest(message);
		
		return null;
	}

	@Override
	public Any getClassSchedule(String studentId) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add(studentId);
		
		String message = joiner.toString();		
		FrontEndUtitlies.sendUDPRequest(message);
		
		return null;
	}

	@Override
	public boolean dropCourse(String studentId, String courseId) {
		
		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add(studentId)
				.add(courseId);
		
		String message = joiner.toString();		
		FrontEndUtitlies.sendUDPRequest(message);
		
		return false;
	}

	@Override
	public Any swapCourse(String studentId, String newCourseId, String oldCourseId) {

		StringJoiner joiner = new StringJoiner("&")
				.add(server)
				.add(studentId)
				.add(newCourseId)
				.add(oldCourseId);
		
		String message = joiner.toString();		
		FrontEndUtitlies.sendUDPRequest(message);
		
		return null;
	}	
	
}