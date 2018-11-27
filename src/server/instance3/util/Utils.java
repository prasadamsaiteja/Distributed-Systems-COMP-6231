/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package server.instance3.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;

/**
 * Utility class
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
 *
 */
public class Utils {

	/**
	 * Validates a User
	 * 
	 * @param id
	 * @param userRole
	 * @param department
	 * @return
	 */
	public static SimpleEntry<Boolean, String> validateUser(final String id, final Role userRole,
			final Department department) {
		String dept, role, value;
		// string length !=9
		if (id.length() != 9)
			return new SimpleEntry<Boolean, String>(false, "Seems to be an invalid id(length not equal to 9).");

		dept = id.substring(0, 4).toUpperCase();
		role = id.substring(4, 5).toUpperCase();
		value = id.substring(5);

		// validate department
		if (!departmentMatch(dept))
			return new SimpleEntry<Boolean, String>(false, "The department('" + dept + "') isn't recognized.");
		else if (department != null && department != Department.valueOf(dept))
			return new SimpleEntry<Boolean, String>(false,
					"You are not authorized for this department('" + dept + "').");
		else if (!roleMatch(role))
			return new SimpleEntry<Boolean, String>(false, "The role('" + role + "') isn't correct.");
		else if (role != null && userRole != Role.fromString(role)) {
			return new SimpleEntry<Boolean, String>(false, "This operation is invalid for an advisor('" + role + "').");
		}

		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return new SimpleEntry<Boolean, String>(false, "The id('" + value + "') isn't correct.");
		}

		return new SimpleEntry<Boolean, String>(true, "valid");
	}

	/**
	 * Validates a course
	 * 
	 * @param courseId
	 * @return
	 */
	public static SimpleEntry<Boolean, String> validateCourse(final String courseId) {
		return validateCourse(courseId, null);

	}

	public static SimpleEntry<Boolean, String> validateCourse(final String courseId, Department department) {

		if (courseId.length() != 8)
			return new SimpleEntry<Boolean, String>(false, "Seems to be an invalid course(length not equal to 8).");
		String dept, value;

		dept = courseId.substring(0, 4).toUpperCase();
		value = courseId.substring(4);

		if (!Utils.departmentMatch(dept))
			return new SimpleEntry<Boolean, String>(false, "The department('" + dept + "') isn't recognized.");
		else if (department != null && department != Department.valueOf(dept))
			return new SimpleEntry<Boolean, String>(false,
					"You are not authorized for this department('" + dept + "').");
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return new SimpleEntry<Boolean, String>(false, "Course id('" + value + "') isn't valid.");
		}

		return new SimpleEntry<Boolean, String>(true, "valid");
	}

	/**
	 * Validates a semester
	 * 
	 * @param semester
	 * @return
	 */
	public static SimpleEntry<Boolean, String> validateSemester(String semester) {
		boolean status = Semester.isValidSemester(semester);
		String msg = null;
		if (!status)
			msg = semester + " isn't valid semester.";
		return new SimpleEntry<Boolean, String>(status, msg);
	}

	/**
	 * Converts from object to byte array
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] objectToByteArray(Object obj) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(byteOut);
			out.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return byteOut.toByteArray();
	}

	/**
	 * converts from byte array to object
	 * 
	 * @param data
	 * @return
	 */
	public static Object byteArrayToObject(byte[] data) {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		Object result = null;
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(byteIn);
			result = (Object) in.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Case insensitive match for department.
	 * 
	 * @param dept
	 * @return true|false
	 */
	public static boolean departmentMatch(final String dept) {
		return dept.matches("(?i)COMP|SOEN|INSE");
	}

	/**
	 * Case insensitive match for use role.
	 * 
	 * @param role
	 * @return true|false
	 */
	public static boolean roleMatch(final String role) {
		return role.matches("(?i)A|S");
	}

}
