package server.instance4.pojo;

/**
 * For storing the reusable constants
 * 
 * @author raghavdutta
 *
 */
public final class Constants {

	public static final String OP_HARDWARE_CRASH = "hardwareCrash";
	public static final String OP_ISALIVE = "isAlive";
	public static final String OP_GETSTATE = "getState";
	public static final String OP_SETSTATE = "setState";
	
	public static final String ADVISOR_LOGS_DIRECTORY = "src/main/resources/logs/advisor/";
	public static final String STUDENT_LOGS_DIRECTORY = "src/main/resources/logs/student/";
	public static final String SERVER_LOGS_DIRECTORY = "src/main/resources/logs/server/";
	public static final String BASE_URL = "http://localhost:8080/";
	public static final String BASE_URL_COMP = "http://localhost:8080/";
	public static final String BASE_URL_INSE = "http://localhost:8181/";
	public static final String BASE_URL_SOEN = "http://localhost:8282/";
	public static final String WSDL = "?wsdl";
	public static final String UNDERSCORE = "_";
	public static final String EMPTY_STRING = "";
	public static final String CAPACITY = "capacity";
	public static final String STUDENTS_ENROLLED = "studentsEnrolled";
	public static final String STUDENT_IDS = "studentIds";
	public static final String STUDENT_ID = "studentId";
	public static final String COURSE_ID = "courseId";
	public static final String NEW_COURSE_ID = "newCourseId";
	public static final String OLD_COURSE_ID = "oldCourseId";
	public static final String OLD_COURSE_DEPT = "oldCourseDept";
	public static final String SEMESTER = "semester";
	public static final String LOG_MSG = "METHOD[%s]; PARAMETERS%s; STATUS[%s]; SERVER_MESSAGE[%s]";
	public static final String OPERATION_ADD_COURSE = "addCourse";
	public static final String OPERATION_REMOVE_COURSE = "removeCourse";
	public static final String OPERATION_LIST_COURSE_AVAILABILITY = "listCourseAvailability";
	public static final String OPERATION_ENROL_COURSE = "enrolCourse";
	public static final String OPERATION_GET_CLASS_SCHEDULE = "getClassSchedule";
	public static final String OPERATION_DROP_COURSE = "dropCourse";
	public static final String OPERATION_SWAP_COURSE = "swapCourse";
	public static final int MAX_ELECTIVE_COURSES = 2;
	public static final int MAX_COURSES_TAKEN_BY_STUDENT = 3;
	public static final String COMP = "COMP";
	public static final String INSE = "INSE";
	public static final String SOEN = "SOEN";
}
