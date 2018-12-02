package server.instance2.dcrsimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import server.instance2.dcrsinterface.DCRSInterface;
import server.instance2.logging.LogManager;
import utils.Config;
import utils.UDPUtilities;


public class Impl implements DCRSInterface{

	String department;
	
	static LogManager logManager=null;
	static public HashMap<String,HashMap<String,HashMap<String,Object>>> recordDetails=new HashMap<>();
	

	
	 public Impl(String dept)  {
		//super();
		this.department = dept.toUpperCase();
		try {
			logManager = new LogManager(dept+".log");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	public boolean addCourse(String advisorId,String courseID, String semester, int capacity) {
		
		if(recordDetails==null) {
			HashMap<String, HashMap<String, java.lang.Object>> subMap=new HashMap<>();
			HashMap<String, java.lang.Object> detailsMap=new HashMap<>();
			java.lang.Object obj=capacity;
			synchronized(this) {
				detailsMap.put("capacity", obj);			
				subMap.put(courseID,detailsMap);				
				recordDetails.put(semester, subMap);
			}

		}
		else {

			if(recordDetails.get(semester)!=null) {
				HashMap<String, HashMap<String, java.lang.Object>> subMap=recordDetails.get(semester);
				HashMap<String, java.lang.Object> detailsMap=new HashMap<>();
				java.lang.Object obj=capacity;
				synchronized(this) {
					detailsMap.put("capacity", obj);
					if(!subMap.containsKey(courseID)) {
						subMap.put(courseID,detailsMap);
					}
					else {
						logManager.writeLog("Request Failed");
						return false;

					}
					recordDetails.put(semester, subMap);
				}
			}
			else {
				HashMap<String, HashMap<String, java.lang.Object>> subMap=new HashMap<>();
				HashMap<String, java.lang.Object> detailsMap=new HashMap<>();
				synchronized(this) {
					detailsMap.put("capacity", capacity);
					subMap.put(courseID,detailsMap);

					recordDetails.put(semester, subMap);
				}
			}

		}
		logManager.writeLog("Request Succeeded");
		return true;
	}

	
	public boolean removeCourse(String advisorId,String courseID, String semester) {
		if(recordDetails==null) {
			logManager.writeLog("Request Failed");
			return false;
		}
		else {

			if(recordDetails.get(semester)!=null) {
				HashMap<String, HashMap<String, java.lang.Object>> subMap=recordDetails.get(semester);
				synchronized(this) {		
					for(Map.Entry<String, HashMap<String,java.lang.Object>> eMap:subMap.entrySet()) {
						if(eMap.getKey().equalsIgnoreCase(courseID)) {

							/*HashMap<String,java.lang.Object> detMap=subMap.get(courseID);*/

							subMap.remove(courseID);
							recordDetails.put(semester, subMap);
							logManager.writeLog("Request Succeeded");
							return true;
						}

					}

				}
			}
			else {
				logManager.writeLog("Request Failed");
				return false;
			}

		}
		//	System.out.println("AFTER REMOVE COURSE:"+recordDetails);


		return false;
	}

	
	public SimpleEntry<Boolean,String> enrolCourse(String studentID, String courseID, String semester) {
		
		String status="Failure";
	//	System.out.println("coming");
		SimpleEntry<Boolean,String> message = new SimpleEntry<Boolean, String>(false, "course enrollment unsuccessful");
		try {
			if(!(returnSemesterCountForStudent(studentID, semester)<3)) {
				logManager.writeLog("Request Failed due to course limit exceeding per sem");
				message = new SimpleEntry<Boolean, String>(false, "course enrollment unsuccessful");
				return message;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(department.equalsIgnoreCase("comp"))
			status=commonEnrolmentFunctionality(studentID,courseID,semester,"comp","soen","inse");
		if(department.equalsIgnoreCase("soen"))
			status=commonEnrolmentFunctionality(studentID,courseID,semester,"soen","inse","comp");
		if(department.equalsIgnoreCase("inse"))
			status=commonEnrolmentFunctionality(studentID,courseID,semester,"inse","soen","comp");
		if(status.contains("success")) {
			message = new SimpleEntry<Boolean, String>(true, "course enrollment successful");
		}
		
		return message;
	}

	
	public boolean dropCourse(String studentID, String courseID) {

		logManager.writeLog("REQUEST TYPE:dropcourse"+"parameters:"+studentID+","+courseID);
		String status="Failure";
		if(department.equalsIgnoreCase("comp")) {
			try {
			String dummy="dropcourse"+":"+studentID+":"+courseID;
			UDPClient udpClient=new UDPClient();
			String response1=new String();
			if(courseID.contains("SOEN")) {
				response1 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT("SOEN"))));
			//	response1=udpClient.udpRequest("SOEN", dummy);
				if(response1.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
			if(courseID.contains("INSE")) {
				response1 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT("INSE"))));
				//response1=udpClient.udpRequest("INSE", dummy);
				if(response1.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}}
			catch(Exception e) {
				e.printStackTrace();
			}
			if(courseID.contains("COMP")) {
				String response4=dropcourseinowndept( studentID, courseID);
				if(response4.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
		}
		else if(department.equalsIgnoreCase("inse")) {
			String dummy="dropcourse"+":"+studentID+":"+courseID;
			UDPClient udpClient=new UDPClient();
			String response1=new String();
			try {
			if(courseID.contains("SOEN")) {
				response1 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT("SOEN"))));
			//	response1=udpClient.udpRequest("SOEN", dummy);
				if(response1.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
			if(courseID.contains("COMP")) {
				response1 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT("COMP"))));
			//	response1=udpClient.udpRequest("COMP", dummy);
				if(response1.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			if(courseID.contains("INSE")) {
				String response4=dropcourseinowndept( studentID, courseID);
				if(response4.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
		}
		else if(department.equalsIgnoreCase("soen")) {
			String dummy="dropcourse"+":"+studentID+":"+courseID;
			UDPClient udpClient=new UDPClient();
			String response1=new String();
			try {
			if(courseID.contains("INSE")) {
				response1 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT("INSE"))));
			//	response1=udpClient.udpRequest("INSE", dummy);
				if(response1.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
			if(courseID.contains("COMP")) {
				response1 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT("COMP"))));
			//	response1=udpClient.udpRequest("COMP", dummy);
				if(response1.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
			}catch(Exception e) {
				e.printStackTrace();
			}
			if(courseID.contains("SOEN")) {
				String response4=dropcourseinowndept( studentID, courseID);
				if(response4.toString().equals("success")) {
					logManager.writeLog("Request Succeeded");
					status="success";
				}
				else {
					logManager.writeLog("Request Failed");
					status="Failure";
				}
			}
		}
		
		if(status.equalsIgnoreCase("success")) {
			return true;
		}
		else if(status.equalsIgnoreCase("failure")){
			return false;
		}
		
		return false;
	}

	
	public HashMap<String, Integer> listCourseAvailability(String advisorId,String semester) {
		
	//	String listCourses= new String();
		HashMap<String, Integer> availableCourseList=new HashMap<>();
		if(department.equalsIgnoreCase("comp")) {
			availableCourseList=commonListCourseFunctionality(semester, "SOEN", "INSE");
			
		}

		else if(department.equalsIgnoreCase("inse")) {
			availableCourseList=commonListCourseFunctionality(semester, "SOEN", "COMP");
		}
		else if(department.equalsIgnoreCase("soen")) {
			availableCourseList=commonListCourseFunctionality(semester, "COMP", "INSE");
		}

		/*for(Map.Entry<String, Integer> map:availableCourseList.entrySet()) {
			listCourses+=map.getKey()+":"+String.valueOf(map.getValue());
		}*/
		return availableCourseList;
	}

	
	public HashMap<String, ArrayList<String>> getClassSchedule(String studentId) {

		HashMap<String, ArrayList<String>> overallSchedule=null;

		String classSchedule=new String();


		if(department.equalsIgnoreCase("comp")) {

			overallSchedule=commonGetClassSchedule(overallSchedule, studentId, "SOEN", "INSE");

		}
		else if(department.equalsIgnoreCase("inse")) {
			overallSchedule=commonGetClassSchedule(overallSchedule, studentId, "SOEN", "COMP");
		}
		else if(department.equalsIgnoreCase("soen")) {
			overallSchedule = commonGetClassSchedule(overallSchedule, studentId, "INSE", "COMP");
		}

	//	System.out.println("Original map:"+overallSchedule);
		for(Map.Entry<String, ArrayList<String>> map:overallSchedule.entrySet()) {

			classSchedule+=map.getKey()+":"+map.getValue().toString()+" ";
		}

		return overallSchedule;	

	}

	
	public SimpleEntry<Boolean,String> swapCourse(String studentID, String newCourseID, String oldCourseID) {


		
		logManager.writeLog("REQUEST TYPE:swapCourse,Parameters:"+studentID+","+newCourseID+","+oldCourseID);
		SimpleEntry<Boolean, String> message =new SimpleEntry<Boolean, String>(true, "course swapped successfully");

	//	String message="swap operation failed";
		//check if old course exist

		if(doesOldcourseexist(studentID,oldCourseID,department).getKey()) {
		//	System.out.println("old course exists");
			String sem=doesOldcourseexist(studentID,oldCourseID,department).getValue();
		//	System.out.println(placeAvailableinNewCourse(studentID,newCourseID,department));
			if(sem !=null && placeAvailableinNewCourse(studentID,newCourseID,department)) {
				//System.out.println("new course available");
				synchronized (this) {
					String enrolment=null;
					boolean dropResult=dropCourse(studentID, oldCourseID);
					if(dropResult) {
						//check if available space in new course
						enrolment=enrolCourse(studentID, newCourseID, sem).getValue().toString();
						if(enrolment!=null && enrolment.equalsIgnoreCase("success")) {
							logManager.writeLog("course swapped successfully");	
							message =new SimpleEntry<Boolean, String>(true, "course swapped successfully");
						//	message="course swapped successfully";
						}else {
						//	message="enrolment failure";
							message =new SimpleEntry<Boolean, String>(true, "course swapping failed due to enrolment issue");
							//if enrolment is failed, then drop should not happen and hence student is enrolled again into the old course here
							enrolCourse(studentID, oldCourseID, sem);
						}
					}
					else {
						message =new SimpleEntry<Boolean, String>(true, "course swapping failure");
						
					}

				}

			}
			else {
				logManager.writeLog("swap operation failed");
			}

		}

		
		return message;
	
	}

	
	
	
	/////helper methods
	
	
	public String fetchUDPPORT(String dep) {
		
		if(dep.equalsIgnoreCase("SOEN")) {
			return "INSTANCE2_SOEN_PORT";
		}
		else if(dep.equalsIgnoreCase("COMP")) {
			return "INSTANCE2_COMP_PORT";
		}
		else if(dep.equalsIgnoreCase("INSE")) {
			return "INSTANCE2_INSE_PORT";
		}
		return null;
	}
	
	public  String commonEnrolmentFunctionality(String studentID, String courseID, String semester, String d1,String d2,String d3)  {
		String dummy="enrolcourse"+":"+semester+":"+studentID+":"+courseID;
		String dummy2="getOtherCourseCount"+":"+studentID;
		UDPClient udpClient=new UDPClient();

		String countStr=new String();
		int othrCount=0;
		String response1=new String();
		if(courseID.toLowerCase().contains(d2)) {
			try {
				countStr =(String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy2,Config.getConfig(fetchUDPPORT(d3.toUpperCase()))));
				String checkCount = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy2,Config.getConfig(fetchUDPPORT(d2.toUpperCase()))));
				if(checkCount!=null && Integer.parseInt(checkCount)>0) {
				//	System.out.println("count for d3:"+checkCount);
					othrCount+=Integer.parseInt(checkCount);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//countStr=udpClient.udpRequest(d2.toUpperCase(), dummy2);
			if(countStr!=null && Integer.parseInt(countStr)>0) {
				//System.out.println("count for d2:"+countStr);
				othrCount+=Integer.parseInt(countStr);
			}
		//	System.out.println("count for d3:"+countStr);
			if(countStr!=null && Integer.parseInt(countStr)>=0) {
				othrCount+=Integer.parseInt(countStr);
			//	System.out.println("othrCount:"+othrCount);
				if(othrCount>=2) {
					logManager.writeLog("Failure due to excess of other dept courses");
					return "Failure due to excess of other dept courses";
				}
			}
			//response1=udpClient.udpRequest(d3.toUpperCase(), dummy);
				try {
					response1= (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT(d2.toUpperCase()))));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//	System.out.println("Response from SOEN enrolment:"+response1+":"+countStr);
			//	logManager.logger.log("Response from SOEN enrolment:"+response1+":"+countStr);
			if(response1.toString().equals("success")) {
				logManager.writeLog("Request Succeeded");
				return "success";						

			}
			else {
				logManager.writeLog("Request Failed");
				return "Failure";
			}

		}
		
		if(courseID.toLowerCase().contains(d3)) {
			try {
				countStr =(String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy2,Config.getConfig(fetchUDPPORT(d2.toUpperCase()))));
				String checkCount = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy2,Config.getConfig(fetchUDPPORT(d3.toUpperCase()))));
				if(checkCount!=null && Integer.parseInt(checkCount)>0) {
				//	System.out.println("count for d3:"+checkCount);
					othrCount+=Integer.parseInt(checkCount);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//countStr=udpClient.udpRequest(d2.toUpperCase(), dummy2);
			if(countStr!=null && Integer.parseInt(countStr)>0) {
				//System.out.println("count for d2:"+countStr);
				othrCount+=Integer.parseInt(countStr);
			}
		//	System.out.println("count for d3:"+countStr);
			if(countStr!=null && Integer.parseInt(countStr)>=0) {
				othrCount+=Integer.parseInt(countStr);
			//	System.out.println("othrCount:"+othrCount);
				if(othrCount>=2) {
					logManager.writeLog("Failure due to excess of other dept courses");
					return "Failure due to excess of other dept courses";
				}
			}
			//response1=udpClient.udpRequest(d3.toUpperCase(), dummy);
				try {
					response1= (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT(d3.toUpperCase()))));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//	System.out.println("Response from SOEN enrolment:"+response1+":"+countStr);
			//	logManager.logger.log("Response from SOEN enrolment:"+response1+":"+countStr);
			if(response1.toString().equals("success")) {
				logManager.writeLog("Request Succeeded");
				return "success";						

			}
			else {
				logManager.writeLog("Request Failed");
				return "Failure";
			}

		}
		
		if(courseID.toLowerCase().contains(d1)) {
			response1=enrolCourseinowndept(studentID, courseID, semester);
			if(response1.toString().equals("success")) {

				logManager.writeLog("Request Succeeded");
				return "success";
			}
			else {
				logManager.writeLog("Request Failed");
				return "Failure";
			}
		}

		return "Failure";

	}



	public int returnSemesterCountForStudent(String studentID, String sem) throws FileNotFoundException, IOException  {
		int count=0;
		if(studentID.contains("COMP")) {
			String dummy="semcount"+":"+sem+":"+studentID;
			UDPClient udpClient=new UDPClient();
			/*String soenthing = udpClient.udpRequest("SOEN", dummy);
			System.out.println(soenthing);*/
			//System.out.println("What is th object coming here?:"+udpClient.sendRequest(dummy,Config.getConfig("INSTANCE2_COMP_PORT")));
			String soenthing = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig("INSTANCE2_SOEN_PORT")));
			if(soenthing == null) {
				soenthing ="0";
			}
			count+=Integer.parseInt(soenthing);
			String insething = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig("INSTANCE2_INSE_PORT")));
			if(insething == null) {
				insething ="0";
			}
			count+=Integer.parseInt(insething);
			//count+=Integer.parseInt(udpClient.udpRequest("INSE", dummy));
			count+=returnDeptSemesterCountForStudent(studentID, sem);

		}
		else if(studentID.contains("INSE")) {
			String dummy="semcount"+":"+sem+":"+studentID;
			UDPClient udpClient=new UDPClient();
			String soenthing = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig("INSTANCE2_SOEN_PORT")));
			if(soenthing == null) {
				soenthing ="0";
			}
			count+=Integer.parseInt(soenthing);
			String insething = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig("INSTANCE2_COMP_PORT")));
			if(insething == null) {
				insething ="0";
			}
			count+=Integer.parseInt(insething);
			count+=returnDeptSemesterCountForStudent(studentID, sem);

		}
		else if(studentID.contains("SOEN")) {
			String dummy="semcount"+":"+sem+":"+studentID;
			UDPClient udpClient=new UDPClient();
			String soenthing = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig("INSTANCE2_INSE_PORT")));
			if(soenthing == null) {
				soenthing ="0";
			}
			count+=Integer.parseInt(soenthing);
			String insething = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig("INSTANCE2_COMP_PORT")));
			if(insething == null) {
				insething ="0";
			}
			count+=Integer.parseInt(insething);
			count+=returnDeptSemesterCountForStudent(studentID, sem);

		}
		return count;
	}

	public int returnDeptSemesterCountForStudent(String studentID, String sem){
		int count=0;

		if(recordDetails==null)
		{
			return 0;
		}
		HashMap<String,HashMap<String,java.lang.Object>> sMap=recordDetails.get(sem.toUpperCase());

		//	System.out.println("Before enrolment schedule:"+sMap);
		if(sMap!=null) {
			for(Map.Entry<String, HashMap<String,java.lang.Object>> e:sMap.entrySet()) {
				if(e.getValue() !=null) {
					for(Map.Entry<String, java.lang.Object> s:e.getValue().entrySet()) {
						if(s.getKey()!=null &&s.getKey().equals("studentsinCourse")) {
							ArrayList<String> studs=(ArrayList<String>) s.getValue();
							if(studs.size()>0) {
								if(studs.contains(studentID)) {
									count++;
								}
							}
						}
					}
				}
			}

		}
		return count;
	}


	public String enrolCourseinowndept(String studentID, String courseID, String semester)  {

		if(recordDetails==null || recordDetails.keySet().size()==0) {

			return "Failure";
		}
		if(recordDetails.get(semester) ==null) {
			return "Failure";
		}
		//	System.out.println("Records::"+recordDetails);
		if(!checkInOtherSems(studentID, courseID, semester)) {
			return "Failure";
		}
		if(!semQuotaExceedCheck(studentID,semester)) {
			logManager.writeLog("Request Failure due to course limit exceeding");
			return "Failure due to course limit exceeding";
		}
		HashMap<String, HashMap<String, java.lang.Object>> semMap=recordDetails.get(semester);

		HashMap<String,java.lang.Object> courseMap=semMap.get(courseID);
		if(courseMap !=null) {

			String cap=courseMap.get("capacity").toString();
			int capacity=-1;
			List<String> Students=new ArrayList<String>();

			try {
				capacity=Integer.parseInt(cap);
				if(capacity>0) {
					//if students exist in the course
					if(courseMap.containsKey("studentsinCourse")) {
						Students=(List) courseMap.get("studentsinCourse");
						if(Students.size()>0) {
							//student already registered
							if(Students.contains(studentID)) {
								return "Failure:Student already registered";
							}
							// this student is not registered
							else {
								capacity=capacity-1;
								Students.add(studentID);
							}
						}
						else {
							Students.add(studentID);
							capacity=capacity-1;
						}
					}
					//if no student in the course
					else {
						if(!courseMap.containsKey("studentsinCourse")) {
							Students.add(studentID);
							capacity=capacity-1;
						}
					}
					java.lang.Object obj=Students;
					synchronized(this) {
						courseMap.put("capacity", (java.lang.Object) capacity);
						courseMap.put("studentsinCourse", obj);
						semMap.put(courseID, courseMap);
						recordDetails.put(semester, semMap);
					//	System.out.println("after enrolment:"+recordDetails);
						logManager.writeLog("Request Succeeded");
						return "success";
					}
				}
				else {
					logManager.writeLog("Request Failure due to capacity issue");
					return "Failure: CLASS CAPACITY FULL";
				}


			}
			catch(NumberFormatException ne) {
				System.out.println(ne);
			}

		}
		return "Failure";


	}


	public static int returnOtherdepartmentCount(String studentID, String department2) {
		int count=0;
		//System.out.println("check the hashmap:"+recordDetails);
		for(Map.Entry<String, HashMap<String,HashMap<String,java.lang.Object>>> sMap:recordDetails.entrySet())
		{
			if(sMap.getValue() !=null) {
				for(Map.Entry<String,HashMap<String, java.lang.Object>> cMap:sMap.getValue().entrySet()) {
					HashMap<String,java.lang.Object> detMap=cMap.getValue();
					if(detMap.containsKey("studentsinCourse")) {
						List studs=(List)detMap.get("studentsinCourse");

						if(studs.size() >0) {
							if(studs.contains(studentID)) {
								count++;
							}
						}
					}
				}
			}
		}
		//System.out.println(count);
		return count;
	}

	public static  String getOwnClassSchedule(String studentId)  {
		
		//CompServer ser=new CompServer();
		String overallSchedule=new String();

	//	System.out.println("Ownclassschedule::"+recordDetails);
		if(recordDetails!=null) {
			for(Map.Entry<String, HashMap<String,HashMap<String,java.lang.Object>>> semMap:recordDetails.entrySet()) {
				String classSchedule=new String();
				String sem=semMap.getKey();
				classSchedule=sem+":";
				//		System.out.println("Coming insideSem:"+sem);
				ArrayList<String> studSubs=new ArrayList<String>();

				if(semMap!=null) {
					for(Entry<String, HashMap<String, java.lang.Object>> courseMap:semMap.getValue().entrySet()) {
						String courses=new String();
						if(courseMap!=null) {
							String courseId=courseMap.getKey();
							//		System.out.println(courseId);
							HashMap<String,java.lang.Object> detMap=courseMap.getValue();
							if(detMap!=null) {
								List list=(List)detMap.get("studentsinCourse");

								if(list!=null && list.contains(studentId)) {
									studSubs.add(courseId);
									courses=","+courseId;								

								}
							}
						}
						classSchedule+=courses;
					}

				}
				overallSchedule+= " "+classSchedule;
			}
		}
	//	System.out.println(overallSchedule);
		return overallSchedule;
	}



	public static boolean semQuotaExceedCheck(String studentID,String sem) {
		boolean status=false;

		if(recordDetails==null) {
			return false;
		}
		if(recordDetails.get(sem)==null) {
			return false;
		}

		int count=0;
		HashMap<String, HashMap<String, java.lang.Object>> semMap=recordDetails.get(sem);

		for(Map.Entry<String, HashMap<String, java.lang.Object>> sMap:semMap.entrySet()) {

			HashMap<String,java.lang.Object> detMap=sMap.getValue();
			List<String> Students=new ArrayList<String>();
			if(detMap!=null && detMap.containsKey("studentsinCourse")) {

				Students=(List<String>) detMap.get("studentsinCourse");
			//	System.out.println("Check1:"+Students);
				if(Students.contains(studentID)) {
					count++;
				}
			}

		}
		//sem course rate exceeded
		if(count<3) {
			//System.out.println("Sem course count:"+count);
			return true;
		}

		return status;
	}




	private boolean checkInOtherSems(String studentID, String courseID, String semester) {
		

		if(recordDetails!=null) {
			for(Map.Entry<String, HashMap<String,HashMap<String,java.lang.Object>>> sMap:recordDetails.entrySet()) {
				if(sMap.getValue()!=null) {
					for(Map.Entry<String, HashMap<String,java.lang.Object>> cMap:sMap.getValue().entrySet()) {
						if(cMap!=null && cMap.getKey().equals(courseID)) {
							HashMap<String,java.lang.Object> detMap=cMap.getValue();
							if(detMap.containsKey("studentsinCourse")) {
								List studs=(List) detMap.get("studentsinCourse");
								if(studs.contains(studentID)) {
									return false;
								}
							}
						}
					}
				}
			}
		}


		return true;
	}

	public String dropcourseinowndept(String studentID, String courseID) {

		String status="Failure";
		//	System.out.println("BEFORE DROPPING:"+recordDetails);
		if(recordDetails!=null) {

			for(Map.Entry<String, HashMap<String,HashMap<String,java.lang.Object>>> sMap:recordDetails.entrySet()) {
				HashMap<String,HashMap<String,java.lang.Object>> c1Map=sMap.getValue();
				if(c1Map!=null) {
					for(Map.Entry<String, HashMap<String,java.lang.Object>> cMap:c1Map.entrySet() ) {
						if(cMap.getKey().equals(courseID)) {
							HashMap<String,java.lang.Object> detMap=cMap.getValue();
							if(detMap.containsKey("studentsinCourse")) {
								List students=(List) detMap.get("studentsinCourse");
								int capacity=-1;
								if(students.contains(studentID)) {
									students.remove(studentID);
									if(detMap.containsKey("capacity")) {
										String capcity=detMap.get("capacity").toString();
										if(capcity !=null && !capcity.isEmpty()) {
											capacity=Integer.parseInt(capcity)+1;
											detMap.put("capacity", String.valueOf(capacity));
											detMap.remove(studentID);
											logManager.writeLog("Request Succeeded");
											return "success";
										}

									}
								}
								else {
									logManager.writeLog("Request Failed");
									status="Failure";
								}
							}
						}
						else {
							logManager.writeLog("Request Failed");
							status="Failure";
						}
					}
				}
			}
		}
		//	System.out.println("AFTER DROPPING:"+recordDetails);

		return status;
	}
	public HashMap<String,Integer> commonListCourseFunctionality(String semester,String d1, String d2){

		HashMap<String,Integer> availableCourseList=new HashMap<>(); 
		String dummy="listcourse"+":"+semester;
		UDPClient udpClient=new UDPClient();
		String response1;
		try {
			response1 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT(d1.toUpperCase()))));
			String soenSubs[] =response1.split(",");
			if(soenSubs.length >1&& soenSubs[0].split("=").length>1) {
				for(int i=0;i<soenSubs.length;i++) {
					String subjectId=soenSubs[i].split("=")[0];
					String capacity=soenSubs[i].split("=")[1];
					//		System.out.println(subjectId+"..."+capacity);
					availableCourseList.put(subjectId,Integer.parseInt(capacity.trim()));

				}
			}
			else if(soenSubs.length==1 && soenSubs[0].split("=").length>1) {
				String subjectId=soenSubs[0].split("=")[0];
				String capacity=soenSubs[0].split("=")[1];
				//		System.out.println("CAPACITY..."+capacity);			
				availableCourseList.put(subjectId,Integer.parseInt(capacity.trim()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//String response1=udpClient.udpRequest(d1, dummy);
		//		System.out.println("Response from SOEN:"+response1);
		
		//response1.replaceFirst(",", "");
		//		System.out.println("RESPONSE:"+response1);
		try {
		String response2 = (String) UDPUtilities.byteArrayToObject(udpClient.sendRequest(dummy,Config.getConfig(fetchUDPPORT(d2.toUpperCase()))));
		//String response2=udpClient.udpRequest(d2, dummy);
		String inseSubs[] =response2.split(",");
		//response2.replaceFirst(",", "");
		//	System.out.println("RESPONSE:"+response2);
		if(inseSubs.length >1 && inseSubs[0].split("=").length>1) {
			for(int i=0;i<inseSubs.length;i++) {
				String subjectId=inseSubs[i].split("=")[0];
				String capacity=inseSubs[i].split("=")[1];
				//		System.out.println(subjectId+"..."+capacity);
				availableCourseList.put(subjectId,Integer.parseInt(capacity.trim()));

			}
		}
		else if(inseSubs.length==1&& inseSubs[0].split("=").length>1) {
			String subjectId=inseSubs[0].split("=")[0];
			String capacity=inseSubs[0].split("=")[1];
			//		System.out.println("CAPACITY..."+capacity);			
			availableCourseList.put(subjectId,Integer.parseInt(capacity.trim()));
		}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		if(recordDetails !=null) {
			HashMap<String,HashMap<String,java.lang.Object>> semCourseList=recordDetails.get(semester);
			//	System.out.println(recordDetails);
			if(semCourseList!=null){
				for(Map.Entry<String,HashMap<String,java.lang.Object>> map:semCourseList.entrySet()) {

					HashMap<String,java.lang.Object> detailsMap=map.getValue();
					String capacity=detailsMap.get("capacity").toString();
					//		System.out.println("sub:"+map.getKey());
					availableCourseList.put(map.getKey(),Integer.parseInt(capacity));

				}
			}
		}
		return availableCourseList;

	
	}
	private HashMap<String, ArrayList<String>> commonGetClassSchedule(HashMap<String, ArrayList<String>> overallSchedule, String studentId,String d1,String d2) {


		overallSchedule=new HashMap<>();
		HashMap<String,ArrayList<String>> commonSchedule=new HashMap<>();
		//	String status="Failure";
		ArrayList<String> fallList=new ArrayList<String>();
		ArrayList<String> winterList=new ArrayList<String>();
		ArrayList<String> summerList=new ArrayList<String>();
		String dummy="classschedule"+":"+studentId;
		UDPClient udpClient=new UDPClient();
		String response1=new String();

		response1=getOwnClassSchedule(studentId);
		//System.out.println("res1:"+response1);
		String response2=new String();
		response2=udpClient.udpRequest(d1, dummy);
		//System.out.println("res2:"+response2);
		//	System.out.println("RESPONSE from SOEN schedule:"+response2);
		String response3=new String();
		response3=udpClient.udpRequest(d2, dummy);
		//System.out.println("res3:"+response3);
		HashMap<String,ArrayList<String>> res1Map=scheduleUtility(response1, overallSchedule,fallList,winterList,summerList);

		HashMap<String,ArrayList<String>> res2Map=scheduleUtility(response2, overallSchedule,fallList,winterList,summerList);
		HashMap<String,ArrayList<String>> res3Map=scheduleUtility(response3, overallSchedule,fallList,winterList,summerList);

		//System.out.println("what happens now?"+overallSchedule);

		return overallSchedule;
	}
	public static HashMap<String,ArrayList<String>> scheduleUtility(String response1,HashMap<String,ArrayList<String>> fullmap,ArrayList<String> fallList,ArrayList<String> winterList,ArrayList<String> summerList){

		String[] compSubs=response1.trim().split(" ");
		for(int i=0;i<compSubs.length;i++) {
			//	System.out.print(","+compSubs[i]);
			String[] words=compSubs[i].split(":");
			String sem=words[0];
			String courses=new String();
			if(words.length>=2 && words[1]!=null && words[1].contains(",")) {
				courses=words[1].replaceFirst(",", "");
			}
			if(sem.equalsIgnoreCase("fall")) {
				//System.out.println(fallList.size());
				if(fallList.size()<3)
					fallList.add(courses);
				fullmap.put(sem, fallList);
			}
			else if(sem.equalsIgnoreCase("winter")) {
				winterList.add(courses);
				fullmap.put(sem, winterList);
			}
			else if(sem.equalsIgnoreCase("summer")) {
				summerList.add(courses);
				fullmap.put(sem, summerList);
			}
		}

		return fullmap;
	}
	private boolean placeAvailableinNewCourse(String studentID, String newCourseID, String department2) {
		String dummy="availablespace"+":"+studentID+":"+newCourseID;
		UDPClient udpClient=new UDPClient();
		String studDept=studentID.substring(0, 4);
		String courseDept=newCourseID.substring(0, 4);
		String response=null;
		if(studDept.equalsIgnoreCase(courseDept)) {
			response=checkSpaceAvailability(newCourseID);
		}
		else {
			response=udpClient.udpRequest(courseDept,dummy);
		}
		if(response!=null && response.equalsIgnoreCase("success")) {
			return true;
		}
		else {
			return false;
		}

	}

	public static String checkSpaceAvailability(String newCourseID) {
		//System.out.println("at availability check:"+recordDetails);
		if(recordDetails==null || recordDetails.keySet().size()==0) {
			return "Failure";
		}
		for(Map.Entry<String, HashMap<String,HashMap<String,java.lang.Object>>> rMap:recordDetails.entrySet())

		{
			if(rMap.getValue() !=null) {
				for(Map.Entry<String, HashMap<String,java.lang.Object>> cMap:rMap.getValue().entrySet()) {
					if(cMap.getKey() !=null) {
						if(cMap.getKey().equalsIgnoreCase(newCourseID)) {
							for(Map.Entry<String, java.lang.Object> eMap:cMap.getValue().entrySet()) {
								if(eMap.getKey().equalsIgnoreCase("capacity")) {
									if(eMap.getValue() !=null && !eMap.getValue().toString().isEmpty()) {
										String capacity=eMap.getValue().toString();
										//System.out.println("capacity"+capacity);
										if(Integer.parseInt(capacity)>0) {
											return "success";
										}

									}
								}
							}
						}
					}
				}
			}
			else {
				return "Failure";
			}

		}
		return "Failure";
	}


	private Map.Entry<Boolean, String> doesOldcourseexist(String studentID, String oldCourseID,String dept) {

		Map.Entry<Boolean, String> entry=new SimpleEntry(false, null);

		String dummy="oldcourse"+":"+studentID+":"+oldCourseID;
		UDPClient udpClient=new UDPClient();
		String studDept=studentID.substring(0, 4);
		String courseDept=oldCourseID.substring(0, 4);
		String response=null;

		if(studDept.equalsIgnoreCase(courseDept)) {
			response=checkifoldcourseforowndept(studentID, oldCourseID, dept);

		}
		else {
			response=udpClient.udpRequest(courseDept, dummy);
		}
		if(response!=null &&  response.split(":")[0] !=null && response.split(":")[0].equalsIgnoreCase("success")) {
			if(response.split(":")[1] !=null) {
				String sem=response.split(":")[1];
				if(sem.toUpperCase().matches("FALL|WINTER|SUMMER"))

					entry=new SimpleEntry(true, sem);
				else {
					entry=new SimpleEntry(false, null);
				}
			}
			else {
				entry=new SimpleEntry(false, null);
			}
			return entry;
		}
		else {
			entry=new SimpleEntry(false, null);
			return entry;
		}

	}

	public static String checkifoldcourseforowndept(String studentID, String oldCourseID,String dept) {

		if(recordDetails==null || recordDetails.keySet().size()==0) {
			return "Failure";
		}
		String sem=null;
		for(Map.Entry<String, HashMap<String,HashMap<String,java.lang.Object>>> rMap:recordDetails.entrySet())

		{
			if(rMap.getValue() !=null) {
				for(Map.Entry<String, HashMap<String,java.lang.Object>> cMap:rMap.getValue().entrySet()) {
					if(cMap.getKey() !=null) {
						if(cMap.getKey().equalsIgnoreCase(oldCourseID)) {
							sem=rMap.getKey();
							for(Map.Entry<String, java.lang.Object> eMap:cMap.getValue().entrySet()) {
								if(eMap.getKey().equalsIgnoreCase("studentsinCourse")) {
									List studs=(List) eMap.getValue();
									if(studs.size() >0 && studs.contains(studentID)) {
										return "success"+":"+sem;
									}
								}
							}
						}
					}
				}
			}
		}
		return "Failure";
	}

	public HashMap<String, HashMap<String, HashMap<String, Object>>> getRecordDetails() {
		
		return recordDetails;
	}

}