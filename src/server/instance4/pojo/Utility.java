package server.instance4.pojo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;

public class Utility {

	/**
	 * Validating user
	 * 
	 * @param id         - id of user
	 * @param userType   - User Type
	 * @param department - department
	 * @return <code> SimpleEntry<Boolean, String></code>
	 */
	public static SimpleEntry<Boolean, String> validateUser(final String id, final UserType userType,
			final Department department) {
		String dept, user_Type, value;
		// string length !=9
		if (id.length() != 9)
			return new SimpleEntry<Boolean, String>(false, "Invalid id (length not equal to 9).");

		dept = id.substring(0, 4).toUpperCase();
		user_Type = id.substring(4, 5).toUpperCase();
		value = id.substring(5);

		// validate department
		if (!departMatch(dept))
			return new SimpleEntry<Boolean, String>(false, "Given department('" + dept + "') is unrecognized.");
		else if (department != null && department != Department.valueOf(dept))
			return new SimpleEntry<Boolean, String>(false,
					"User is unauthorized for given department('" + dept + "').");
		else if (!userTypeMatch(user_Type))
			return new SimpleEntry<Boolean, String>(false, "User type ('" + user_Type + "') is incorrect.");
		else if (user_Type != null && userType != UserType.fromString(user_Type)) {
			return new SimpleEntry<Boolean, String>(false,
					"Given operation is invalid for an advisor('" + user_Type + "').");
		}

		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return new SimpleEntry<Boolean, String>(false, "The id('" + value + "') is incorrect.");
		}

		return new SimpleEntry<Boolean, String>(true, "valid");
	}

	/**
	 * Validating course
	 * 
	 * @param courseId - given courseId
	 * @return <code> SimpleEntry<Boolean, String></code>
	 */
	public static SimpleEntry<Boolean, String> validateCourse(final String courseId) {
		return validateCourse(courseId, null);

	}

	public static SimpleEntry<Boolean, String> validateCourse(final String courseId, Department department) {

		if (courseId.length() != 8)
			return new SimpleEntry<Boolean, String>(false, "Invalid course (length not equal to 8).");
		String dept, value;

		dept = courseId.substring(0, 4).toUpperCase();
		value = courseId.substring(4);

		if (!Utility.departMatch(dept))
			return new SimpleEntry<Boolean, String>(false, "Given department('" + dept + "') is unrecognized.");
		else if (department != null && department != Department.valueOf(dept))
			return new SimpleEntry<Boolean, String>(false,
					"User is not authorized for given department('" + dept + "').");
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return new SimpleEntry<Boolean, String>(false, "Course id('" + value + "') is invalid.");
		}

		return new SimpleEntry<Boolean, String>(true, "valid");
	}

	/**
	 * Validating given semester
	 * 
	 * @param semester - given semester
	 * @return <code> SimpleEntry<Boolean, String></code>
	 */
	public static SimpleEntry<Boolean, String> validateSemester(String semester) {
		boolean status = Semester.isValidSemester(semester);
		String msg = null;
		if (!status)
			msg = semester + " is invalid semester.";
		return new SimpleEntry<Boolean, String>(status, msg);
	}

	/**
	 * Transforming from object to byte array
	 * 
	 * @param obj - object
	 * @return <code>byte[]</code>
	 */
	public static byte[] objectToByteArray(Object obj) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream outputStream;
		try {
			outputStream = new ObjectOutputStream(byteOut);
			outputStream.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteOut.toByteArray();
	}

	/**
	 * Transforming from byte array to object
	 * 
	 * @param data
	 * @return
	 */
	public static Object byteArrayToObject(byte[] data) {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		Object result = null;
		ObjectInputStream inputStream;
		try {
			inputStream = new ObjectInputStream(byteIn);
			result = (Object) inputStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Case insensitive match for department.
	 * 
	 * @param department
	 * @return boolean - true or false
	 */
	public static boolean departMatch(final String department) {
		return department.matches("(?i)COMP|SOEN|INSE");
	}

	/**
	 * Case insensitive match for User types.
	 * 
	 * @param role
	 * @return true|false
	 */
	public static boolean userTypeMatch(final String role) {
		return role.matches("(?i)A|S");
	}

}
