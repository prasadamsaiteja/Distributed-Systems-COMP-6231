package server.instance4.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import server.instance3.util.Semester;
import server.instance4.pojo.Department;
import server.instance4.pojo.Utility;
import utils.Config;
import utils.Constants;
import utils.UDPUtilities;

public class DCRS {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private Department _department;
	private Lock _lock;
	private HashMap<String, HashMap<String, HashMap<String, Object>>> _departmentDatabase;

	/**
	 * Constructor for DCRS
	 * 
	 * @param dept
	 */
	public DCRS(String dept) {
		_department = Department.valueOf(dept);
		_departmentDatabase = new HashMap<>();
		_lock = new ReentrantLock(true);

	}

	/**
	 * Adds course to the department's course list
	 */
	public boolean addCourse(String advisorId, String courseId, String semester, int capacity) {
		boolean status = false;
		String msg = Constants.EMPTY_STRING;
		// locking
		_lock.lock();

		if (_departmentDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = _departmentDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				status = false;
				msg = "Course already exists for " + semester + " semester.";
			} else {
				HashMap<String, Object> courseDetails = new HashMap<>();
				courseDetails.put(Constants.CAPACITY, capacity);
				courseDetails.put(Constants.STUDENTS_ENROLLED, 0);
				courseDetails.put(Constants.STUDENT_IDS, new HashSet<String>());
				courses.put(courseId, courseDetails);
				status = true;
				msg = courseId + " Added.";
			}
		} else {
			// semester is non-existant
			HashMap<String, Object> courseDetails = new HashMap<>();
			courseDetails.put(Constants.CAPACITY, capacity);
			courseDetails.put(Constants.STUDENTS_ENROLLED, 0);
			courseDetails.put(Constants.STUDENT_IDS, new HashSet<String>());
			HashMap<String, HashMap<String, Object>> courses = new HashMap<>();
			courses.put(courseId, courseDetails);

			// synchronizing the write operation to the in-memory database
			_departmentDatabase.put(semester, courses);

			status = true;
			msg = courseId + " Added.";
		}		

		// releasing the lock
		_lock.unlock();

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_ADD_COURSE,
				Arrays.asList(advisorId, courseId, semester, capacity), status, msg));

		return status;
	}

	/**
	 * Removes a course from the department's course list
	 */
	public boolean removeCourse(String advisorId, String courseId, String semester) {

		boolean status = false;
		String msg = Constants.EMPTY_STRING;
		if (_departmentDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = _departmentDatabase.get(semester);
			// locking
			_lock.lock();
			synchronized (courses) {

				if (courses.containsKey(courseId)) {

					courses.remove(courseId);
					status = true;
					msg = courseId + " removed";
				} else {
					status = false;
					msg = semester + " semester doesn't offer this course yet.";
				}
			}
			// releasing the lock
			_lock.unlock();
		} else {
			status = false;
			msg = semester + " semester doesn't have any course yet.";
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_REMOVE_COURSE,
				Arrays.asList(advisorId, courseId, semester), status, msg));

		return status;
	}

	
	/**
	 * Lists the courses available along with the no. of vacant seats for a
	 * particular semester
	 * 
	 * @return
	 */
	public HashMap<String, Integer> listCourseAvailability(String advisorId, String semester) {

		HashMap<String, Integer> result = new HashMap<>();
		result.putAll(listCourseAvailabilityForThisServer(semester));

		// inquire different departments
		for (Department dept : Department.values()) {
			if (dept != _department) {
				result.putAll((HashMap<String, Integer>) Utility.byteArrayToObject(
						udpCommunication(dept, semester, Constants.OP_LIST_COURSE_AVAILABILITY)));
			}
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_LIST_COURSE_AVAILABILITY,
				Arrays.asList(advisorId, semester), result != null, result));

		return result;
	}

	/**
	 * Lists the courses available along with the no. of vacant seats for a
	 * particular semester on this server(COMP|SOEN|INSE)
	 * 
	 * @param semester
	 * @return
	 */
	private HashMap<String, Integer> listCourseAvailabilityForThisServer(String semester) {
		HashMap<String, Integer> result = new HashMap<>();
		// get courses from the current department
		if (_departmentDatabase.containsKey(semester)) {
			_departmentDatabase.get(semester).forEach(
					(course, courseDetails) -> result.put(course, (Integer) courseDetails.get(Constants.CAPACITY)
							- (Integer) courseDetails.get(Constants.STUDENTS_ENROLLED)));
		}

		return result;
	}

	
	/**
	 * Helps to enroll a student in a given course
	 */
	public SimpleEntry<Boolean, String> enrolCourse(String studentId, String courseId, String semester) {

		boolean status = true;
		String msg = null;
		SimpleEntry<Boolean, String> result = null;

		// get student schedule
		HashMap<String, ArrayList<String>> studentSchedule = getClassSchedule(studentId);

		// student already enrolled in 3 courses
		if (studentSchedule.containsKey(semester)
				&& studentSchedule.get(semester).size() >= Constants.MAX_COURSE_TAKEN_BY_STUDENT) {
			status = false;
			msg = studentId + " is already enrolled in " + Constants.MAX_COURSE_TAKEN_BY_STUDENT + " courses "
					+ studentSchedule.get(semester) + " for this " + semester + " semester.";
			return (new SimpleEntry<Boolean, String>(status, msg));
		}

		List<String> departmentCourses = new ArrayList<>();
		List<String> outOfDepartmentCourses = new ArrayList<>();
		studentSchedule.forEach((sem, courses) -> {
			courses.forEach((course) -> {
				Department dept = Department.valueOf(course.substring(0, 4).toUpperCase());
				if (dept == _department)
					departmentCourses.add(course);
				else
					outOfDepartmentCourses.add(course);
			});
		});
		Department courseDept = Department.valueOf(courseId.substring(0, 4).toUpperCase());
		// enroll in this department only
		if (_department == courseDept) {

			// student already taking this course
			if (departmentCourses.contains(courseId)) {
				status = false;
				msg = studentId + " is already enrolled in " + courseId + ".";
			}
			if (status) {
				result = enrollmentForThisDepartment(studentId, courseId, semester);
			}

		} else {

			// check if student is already enrolled in 2 elective courses
			if (outOfDepartmentCourses.size() >= Constants.MAX_ELECTIVE_COURSES) {
				status = false;
				msg = studentId + " is already enrolled in " + Constants.MAX_ELECTIVE_COURSES
						+ " out-of-department courses.";
			} else {
				// enquire respective department
				for (Department dept : Department.values()) {
					if (dept == courseDept) {
						HashMap<String, String> data = new HashMap<>();
						data.put(Constants.STUDENT_ID, studentId);
						data.put(Constants.COURSE_ID, courseId);
						data.put(Constants.SEMESTER, semester);

						result = (SimpleEntry<Boolean, String>) Utility.byteArrayToObject(
								udpCommunication(courseDept, data, Constants.OP_ENROL_COURSE));
					}
				}
			}

			// status = false;
			// msg = "Department not found.";
		}

		if (result == null)
			result = new SimpleEntry<Boolean, String>(status, msg);

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_ENROL_COURSE,
				Arrays.asList(studentId, courseId, semester), result.getKey(), result.getValue()));

		return result;
	}

	private SimpleEntry<Boolean, String> enrollmentForThisDepartment(String studentId, String courseId,
			String semester) {
		boolean status;
		String msg;
		if (_departmentDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = _departmentDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				HashMap<String, Object> courseDetails = courses.get(courseId);

				_lock.lock(); // acquire the lock
				if (((Integer) courseDetails.get(Constants.CAPACITY)
						- (Integer) courseDetails.get(Constants.STUDENTS_ENROLLED)) > 0) {

					status = ((HashSet<String>) courseDetails.get(Constants.STUDENT_IDS)).add(studentId);
					if (status) {
						courseDetails.put(Constants.STUDENTS_ENROLLED,
								(Integer) courseDetails.get(Constants.STUDENTS_ENROLLED) + 1);
						status = true;
						msg = "Enrollment Successful.";
					} else {
						status = false;
						msg = studentId + " is already enrolled in " + courseId + ".";
					}

				} else {
					status = false;
					msg = courseId + " is full.";
				}
				_lock.unlock(); // release the lock
			} else {
				status = false;
				msg = courseId + " is not offered in " + semester + " semester.";
			}
		} else {
			status = false;
			msg = "No Courses avialable for " + semester + " semester.";
		}

		return new SimpleEntry<Boolean, String>(status, msg);
	}

	public HashMap<String, ArrayList<String>> getClassSchedule(String studentId) {
		HashMap<String, ArrayList<String>> schedule = new HashMap<>();
		schedule.putAll(getClassScheduleThisServer(studentId));

		// inquire different departments
		for (Department dept : Department.values()) {
			if (dept != _department) {

				HashMap<String, ArrayList<String>> deptSchedule = (HashMap<String, ArrayList<String>>) Utility
						.byteArrayToObject(udpCommunication(dept, studentId, Constants.OP_GET_CLASS_SCHEDULE));

				for (String semester : deptSchedule.keySet()) {
					if (schedule.containsKey(semester)) {
						schedule.get(semester).addAll(deptSchedule.get(semester));
					} else {
						schedule.put(semester, deptSchedule.get(semester));
					}
				}
			}
		}
		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_GET_CLASS_SCHEDULE, Arrays.asList(studentId),
				schedule != null, schedule));

		return schedule;
	}

	private HashMap<String, ArrayList<String>> getClassScheduleThisServer(String studentId) {
		HashMap<String, ArrayList<String>> schedule = new HashMap<>();
		_departmentDatabase.forEach((semester, courses) -> {
			courses.forEach((course, details) -> {
				if (((HashSet<String>) details.get(Constants.STUDENT_IDS)).contains(studentId)) {
					if (schedule.containsKey(semester)) {
						schedule.get(semester).add(course);
					} else {
						ArrayList<String> temp = new ArrayList<>();
						temp.add(course);
						schedule.put(semester, temp);
					}
				}
			});
		});
		
		for(Semester semester : Semester.values()) {
			if(!schedule.containsKey(semester.name())){
				schedule.put(semester.name(),new ArrayList<String>());
			}
		}
		
		return schedule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remoteObject.EnrollmentInterfaceOperations#dropCourse(java.lang.String,
	 * java.lang.String)
	 */
	public boolean dropCourse(String studentId, String courseId) {

		Department courseDept = Department.valueOf(courseId.substring(0, 4).toUpperCase());
		SimpleEntry<Boolean, String> result;
		if (_department == courseDept) {
			result = dropCourseOnThisServer(studentId, courseId);
		} else {
			HashMap<String, String> data = new HashMap<>();
			data.put(Constants.STUDENT_ID, studentId);
			data.put(Constants.COURSE_ID, courseId);
			result = (SimpleEntry<Boolean, String>) Utility
					.byteArrayToObject(udpCommunication(courseDept, data, Constants.OP_DROP_COURSE));
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_DROP_COURSE,
				Arrays.asList(studentId, courseId), result.getKey(), result.getValue()));
		return result.getKey();
	}

	private SimpleEntry<Boolean, String> dropCourseOnThisServer(String studentId, String courseId) {
		final Map<Boolean, String> temp = new HashMap<>();
		if (_departmentDatabase.size() > 0) {
			_departmentDatabase.forEach((sem, courses) -> {
				if (courses.containsKey(courseId)) {
					courses.forEach((course, courseDetails) -> {
						// locking
						_lock.lock();
						if (course.equals(courseId)) {
							boolean status = ((HashSet<String>) courseDetails.get(Constants.STUDENT_IDS))
									.remove(studentId);
							if (status) {
								courseDetails.put(Constants.STUDENTS_ENROLLED,
										((Integer) courseDetails.get(Constants.STUDENTS_ENROLLED) - 1));
								temp.put(true, "success");
							} else {
								temp.put(false, studentId + " isn't enrolled in " + courseId + ".");
							}
						}
						// releasing the lock
						_lock.unlock();
					});
				} else {
					temp.put(false, courseId + " isn't offered by the department yet.");
				}
			});
		} else {
			temp.put(false, courseId + " isn't offered by the department yet.");
		}

		if (temp.containsKey(true)) {
			return new SimpleEntry<Boolean, String>(true, "Course Dropped.");
		} else {
			return new SimpleEntry<Boolean, String>(false, temp.get(false));
		}
	}

	public SimpleEntry<Boolean, String> swapCourse(String studentId, String newCourseId, String oldCourseId) {

		boolean status = true;
		String msg = null;
		String semester = null;
		SimpleEntry<Boolean, String> result1;
		SimpleEntry<Boolean, String> result2;

		// get students class schedule
		HashMap<String, ArrayList<String>> studentSchedule = getClassSchedule(studentId);
		Department oldCourseDept = Department.valueOf(oldCourseId.substring(0, 4).toUpperCase());
		Department newCourseDept = Department.valueOf(newCourseId.substring(0, 4).toUpperCase());

		List<String> departmentCourses = new ArrayList<>();
		List<String> outOfDepartmentCourses = new ArrayList<>();
		studentSchedule.forEach((sem, courses) -> {
			courses.forEach((course) -> {
				Department dept = Department.valueOf(course.substring(0, 4).toUpperCase());
				if (dept == _department)
					departmentCourses.add(course);
				else
					outOfDepartmentCourses.add(course);
			});
		});

		if (!departmentCourses.contains(oldCourseId) && !outOfDepartmentCourses.contains(oldCourseId)) {
			// check if student is enrolled in oldCourse or not
			status = false;
			msg = studentId + " is not enrolled in " + oldCourseId;
		} else if (departmentCourses.contains(newCourseId) || outOfDepartmentCourses.contains(newCourseId)) {
			// check if the student is already enrolled in newCourseId
			status = false;
			msg = studentId + " is already enrolled in " + newCourseId;
		} else if (newCourseDept != _department && oldCourseDept == _department && outOfDepartmentCourses.size() >= 2) {
			status = false;
			msg = studentId + " is already enrolled in " + Constants.MAX_ELECTIVE_COURSES
					+ " out-of-department courses.";
		}

		if (!status)
			return new SimpleEntry<Boolean, String>(status, msg);

		// get the oldCourseId semester
		outer: for (String semStr : studentSchedule.keySet()) {
			for (String c : studentSchedule.get(semStr)) {
				if (c.equalsIgnoreCase(oldCourseId)) {
					semester = semStr;
					break outer;
				}
			}
		}

		if (newCourseDept == _department) {
			// enrolling in this department, dropping elective or this department course
			// check if new course is offered or not

			// locking
			_lock.lock();
			result2 = checkCouseAvailability(newCourseId, semester);

			if (result2.getKey()) {
				// drop other department course
				boolean temp = dropCourse(studentId, oldCourseId);

				if (temp) {
					// enroll in new course
					result2 = enrollmentForThisDepartment(studentId, newCourseId, semester);

					if (result2.getKey()) {
						status = true;
						msg = Constants.OP_SWAP_COURSE + " successfully";
					} else {
						// ROLLBACK
						enrolCourse(studentId, oldCourseId, semester);
						status = true;
						msg = Constants.OP_SWAP_COURSE + " successfully";
					}
				} else {
					status = result2.getKey();
					msg = result2.getValue();
				}
			} else {
				status = result2.getKey();
				msg = result2.getValue();
			}
			_lock.unlock();// releasing the lock
			// finish

		} else {
			// dropping this dept course, enrolling in elective

			HashMap<String, String> data = new HashMap<>();
			data.put(Constants.STUDENT_ID, studentId);
			data.put(Constants.NEW_COURSE_ID, newCourseId);
			data.put(Constants.OLD_COURSE_ID, oldCourseId);
			data.put(Constants.OLD_COURSE_DEPT, _department.toString());
			data.put(Constants.SEMESTER, semester);

			result2 = (SimpleEntry<Boolean, String>) Utility
					.byteArrayToObject(udpCommunication(newCourseDept, data, Constants.OP_SWAP_COURSE));
			status = result2.getKey();
			msg = result2.getValue();
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_SWAP_COURSE,
				Arrays.asList(studentId, newCourseId, oldCourseId), status, msg));

		return new SimpleEntry<Boolean, String>(status, msg);
	}

	/**
	 * UDP Server for Inter-Departmental communication
	 */
	public void udpServer() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(_department.getUdpPort());
			byte[] buffer = new byte[1000];// to stored the received data from the client.
			// server is always in listening mode.
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				// Server waits for the request to come
				socket.receive(request); // request received

	            byte[] response = processRequest(request, socket);
	            if(response == null)
	            	continue; //will reply to Front end manually

				DatagramPacket reply = new DatagramPacket(response, response.length, request.getAddress(),
						request.getPort()); // reply packet ready
				socket.send(reply); // reply sent
			}
		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}

	private byte[] processRequest(DatagramPacket request, DatagramSocket socket) {
		
		String stringData = new String(request.getData(), 0, request.getLength());
		if (stringData.startsWith("SEQUENCER&")) {
			processSequencerRequest(stringData.replace("SEQUENCER&", ""), socket);
			return null;
		}

		else
			return processUDPRequest(request.getData());
	}
	
	/**
	 * UDP request for information
	 * 
	 * @param data
	 * @return
	 */
	private byte[] processUDPRequest(byte[] data) {
		
		byte[] response = null;
		HashMap<String, Object> request = (HashMap<String, Object>) Utility.byteArrayToObject(data);

		for (String key : request.keySet()) {

			LOGGER.info("Received UDP Socket call for method[" + key + "] with parameters[" + request.get(key) + "]");
			switch (key) {
			case Constants.OP_LIST_COURSE_AVAILABILITY:
				String semester = (String) request.get(key);
				response = Utility.objectToByteArray(listCourseAvailabilityForThisServer(semester));
				break;
			case Constants.OP_ENROL_COURSE:
				HashMap<String, String> info = (HashMap<String, String>) request.get(key);
				response = Utility.objectToByteArray(enrollmentForThisDepartment(info.get(Constants.STUDENT_ID),
						info.get(Constants.COURSE_ID), info.get(Constants.SEMESTER)));
				break;
			case Constants.OP_GET_CLASS_SCHEDULE:
				String studentId = (String) request.get(key);
				response = Utility.objectToByteArray(getClassScheduleThisServer(studentId));
				break;
			case Constants.OP_DROP_COURSE:
				info = (HashMap<String, String>) request.get(key);
				response = Utility.objectToByteArray(
						dropCourseOnThisServer(info.get(Constants.STUDENT_ID), info.get(Constants.COURSE_ID)));
				break;
			case Constants.OP_SWAP_COURSE:
				info = (HashMap<String, String>) request.get(key);
				response = Utility.objectToByteArray(atomicSwapOnCurrentServer(info.get(Constants.STUDENT_ID),
						info.get(Constants.NEW_COURSE_ID), info.get(Constants.OLD_COURSE_ID),
						info.get(Constants.OLD_COURSE_DEPT), info.get(Constants.SEMESTER)));
				break;
				
			case Constants.OP_GETSTATE:
				response = getState();
				break;
			case Constants.OP_SETSTATE:
				HashMap<String, HashMap<String, HashMap<String, Object>>> temp = (HashMap<String, HashMap<String, HashMap<String, Object>>>) request.get(key);
				setState(temp);
				response = String.valueOf(true).getBytes();
				break;
			case Constants.OP_ISALIVE:
				LOGGER.info("\n\n\n\n GOT IT ALIVE REQUEST\n\n\n\n");
				response = String.valueOf(true).getBytes();
				break;
			}
		}

		return response;
	}
	
	private void processSequencerRequest(String receivedRequest, DatagramSocket socket) {
		
		String[] temp = receivedRequest.split("&", 2);
		//int sequenceNumber = Integer.parseInt(temp[0]);
		String request[] = temp[1].trim().split("&");
		byte[] response = new byte[1000];
		
		switch(request[0]){
		
			case "addCourse":
				boolean temp1 = addCourse(request[1], request[2], request[3], Integer.parseInt(request[4]));
				response = UDPUtilities.objectToByteArray(temp1);
				break;
				
			case "removeCourse":
				response = UDPUtilities.objectToByteArray(removeCourse(request[1], request[2], request[3]));
				break;
				
			case "listCourseAvailability":
				response = UDPUtilities.objectToByteArray(listCourseAvailability(request[1], request[2]));
				break;
				
			case "enrolCourse":
				response = UDPUtilities.objectToByteArray(enrolCourse(request[1], request[2], request[3]));
				break;
				
			case "getClassSchedule":
				response = UDPUtilities.objectToByteArray(getClassSchedule(request[1]));
				break;
				
			case "dropCourse":
				response = UDPUtilities.objectToByteArray(dropCourse(request[1], request[2]));
				break;
				
			case "swapCourse":
				response = UDPUtilities.objectToByteArray(swapCourse(request[1], request[2], request[3]));
				break;
		
		}
		
		try {
			
			InetAddress frontendIP = InetAddress.getByName(Config.getStringConfig("FRONTEND_IP"));
			DatagramPacket replyPacket = new DatagramPacket(response, response.length, frontendIP, Config.getConfig("FRONTEND_PORT"));
	        socket.send(replyPacket);
	        
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	private SimpleEntry<Boolean, String> atomicSwapOnCurrentServer(String studentId, String newCourseId,
			String oldCourseId, String oldCourseDept, String semester) {

		SimpleEntry<Boolean, String> result;
		boolean status;
		String msg;

		try {
			// Locking
			_lock.lock();

			result = checkCouseAvailability(newCourseId, semester);
			if (result.getKey()) {
				HashMap<String, String> data = new HashMap<>();
				data.put(Constants.STUDENT_ID, studentId);
				data.put(Constants.COURSE_ID, oldCourseId);
				result = (SimpleEntry<Boolean, String>) Utility.byteArrayToObject(
						udpCommunication(Department.valueOf(oldCourseDept), data, Constants.OP_DROP_COURSE));

				if (result.getKey()) {
					result = enrollmentForThisDepartment(studentId, newCourseId, semester);

					if (result.getKey()) {
						status = true;
						msg = Constants.OP_SWAP_COURSE + " successfully";
					} else {
						// ROLLBACK
						enrolCourse(studentId, oldCourseId, semester);
						status = false;
						msg = Constants.OP_SWAP_COURSE + " unsuccessful";
					}
				} else {
					status = result.getKey();
					msg = result.getValue();
				}
			} else {
				status = result.getKey();
				msg = result.getValue();
			}
			return new SimpleEntry<Boolean, String>(status, msg);
		} finally {
			// Releasing the lock
			_lock.unlock();
		}
	}

	private SimpleEntry<Boolean, String> checkCouseAvailability(String courseId, String semester) {

		boolean status = true;
		String msg = Constants.EMPTY_STRING;
		if (_departmentDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = _departmentDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				HashMap<String, Object> courseDetails = courses.get(courseId);

				if (((Integer) courseDetails.get(Constants.CAPACITY)
						- (Integer) courseDetails.get(Constants.STUDENTS_ENROLLED)) > 0) {
					status = true;

				} else {
					status = false;
					msg = courseId + " is already full.";
				}
			} else {
				status = false;
				msg = courseId + " is not offered in " + semester + " semester.";
			}
		} else {
			status = false;
			msg = "No Courses available for " + semester + " semester.";
		}

		return new SimpleEntry<Boolean, String>(status, msg);
	}

	/**
	 * Creates and sends the UDP request
	 * 
	 * @param dept
	 * @param info
	 * @param method
	 * @return
	 */
	private byte[] udpCommunication(Department dept, Object info, String method) {

		LOGGER.info("Making UDP Socket Call to " + dept + " Server for method : " + method);

		// UDP SOCKET CALL AS CLIENT
		HashMap<String, Object> data = new HashMap<>();
		byte[] response = null;
		data.put(method, info);
		DatagramSocket datagramSocket = null;
		try {
			datagramSocket = new DatagramSocket();
			byte[] message = Utility.objectToByteArray(data);
			InetAddress remoteUdpHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, message.length, remoteUdpHost, dept.getUdpPort());
			datagramSocket.send(request);
			byte[] buffer = new byte[65556];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			datagramSocket.receive(reply);
			response = reply.getData();

		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (datagramSocket != null)
				datagramSocket.close();
		}

		return response;
	}
	
	public byte[] getInternalState() {
		return utils.Utility.deepCopyInstance4State(_departmentDatabase);
	}

	public void setInternalState(byte[] data) {
		this._departmentDatabase = (HashMap<String, HashMap<String, HashMap<String, Object>>>) UDPUtilities
				.byteArrayToObject(data);
	}

	public byte[] getState() {
		return utils.Utility.deepCopyInstance3State(_departmentDatabase);
	}

	/* (non-Javadoc)
	 * @see server.instance3.remoteObject.EnrollmentInterface#setState(byte[])
	 */
	public void setState(HashMap<String, HashMap<String, HashMap<String, Object>>> data) {
		this._departmentDatabase = data;
	}
	
}
