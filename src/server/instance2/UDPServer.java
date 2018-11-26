package server.instance2;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.util.HashMap;
import java.util.Map;




public class UDPServer {
	private Integer SOENPort = 1412;
	private Integer INSEPort = 7777;
	private  Integer COMPPort = 6666;

	static HashMap<String,Integer> serverRepo;


	public UDPServer() {
		super();
		serverRepo=new HashMap<>();
		serverRepo.put("COMP", COMPPort);
		serverRepo.put("INSE",INSEPort);
		serverRepo.put("SOEN",SOENPort);
	}

	static public HashMap<String,HashMap<String,HashMap<String,Object>>> recordDetails;
	private static StringBuilder data(byte[] a) {

		if(a==null)
			return null;
		StringBuilder ret=new StringBuilder();
		int i=0;
		while(a[i] !=0) {

			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}

	DCRSImpl compServer,inseServer;
	DCRSImpl soenServer;

	public  void udpMain(String dept) {
		DatagramSocket COMP = null;
		try {
			if(dept.contains("COMP")) {
				COMP = new DatagramSocket(serverRepo.get(dept));
				compServer=new DCRSImpl(dept);

				recordDetails=compServer.getCourseRecords();
			}
			else if(dept.contains("INSE")) {
				COMP = new DatagramSocket(INSEPort);
				inseServer=new DCRSImpl(dept);

				recordDetails=inseServer.getCourseRecords();
			}
			else if(dept.contains("SOEN")) {
				COMP = new DatagramSocket(SOENPort);
				soenServer=new DCRSImpl(dept);				
				recordDetails=soenServer.getCourseRecords();
			}
			while(true){

				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				COMP.receive(request);
				String bloop=gettheData(buffer,dept,recordDetails);
				byte[] blah = bloop.getBytes();
				DatagramPacket reply = new DatagramPacket(blah, blah.length, request.getAddress(), request.getPort());
				COMP.send(reply);

			}
		}
		catch(SocketException e){
			System.out.println("Socket Exception: "+e);
		}
		catch(IOException e){
			System.out.println("IO Exception: "+e);
		}
		finally{
			if(COMP != null)
				COMP.close();
		}
	}

	private String gettheData(byte[] buffer,String dep, HashMap<String, HashMap<String, HashMap<String, Object>>> recordDetails2) throws IOException {
		// TODO Auto-generated method stub
		String bloop=new String();
		System.out.println("Data Fetched::"+data(buffer).toString());
		if(buffer != null && data(buffer).toString() != null && !data(buffer).toString().isEmpty() )
		{
			if(data(buffer).toString().split(":")[0].equalsIgnoreCase("listcourse")){
				String semester= (data(buffer).toString().split(":")[1]);
				if(recordDetails2 != null) {
					HashMap<String,HashMap<String,Object>> semCourseList=recordDetails2.get(semester);
					if(semCourseList != null) {

						for(Map.Entry<String,HashMap<String,Object>> map:semCourseList.entrySet()) {
							if(bloop.isEmpty()) {
								bloop=map.getKey()+"="+map.getValue().get("capacity"); 
							}
							else {
								bloop =bloop+","+ map.getKey()+"="+map.getValue().get("capacity"); 

							}
						}

					}
				}
			}
			else if(data(buffer).toString().split(":")[0].equalsIgnoreCase("enrolcourse")){
				String sem=(data(buffer).toString().split(":")[1]);
				String studentId=(data(buffer).toString().split(":")[2]);
				String courseId=(data(buffer).toString().split(":")[3]);
				//write a method to check conditions for enrolment with other depts
				DCRSImpl obj=new DCRSImpl(dep.toUpperCase());
				
				bloop=obj.enrolCourseinowndept(studentId, courseId, sem);

			}
			else if(data(buffer).toString().split(":")[0].equalsIgnoreCase("semcount")){
				String sem=(data(buffer).toString().split(":")[1]);
				String studentId=(data(buffer).toString().split(":")[2]);
								
				DCRSImpl obj=new DCRSImpl(dep.toUpperCase());
				
				bloop=String.valueOf(obj.returnDeptSemesterCountForStudent(studentId, sem));

			}
			else if(data(buffer).toString().split(":")[0].equalsIgnoreCase("classschedule")){

				String studentId=(data(buffer).toString().split(":")[1]);

				//write a method to check conditions for enrolment with other depts
				bloop=DCRSImpl.getOwnClassSchedule(studentId);

			}
			else if(data(buffer).toString().split(":")[0].equalsIgnoreCase("getOtherCourseCount")){

				String studentId=(data(buffer).toString().split(":")[1]);
				
				bloop=String.valueOf(DCRSImpl.returnOtherdepartmentCount(studentId,dep));

			}
			else if(data(buffer).toString().split(":")[0].equalsIgnoreCase("dropcourse")){

				String studentId=(data(buffer).toString().split(":")[1]);
				String courseId=(data(buffer).toString().split(":")[2]);
				
				//write a method to check conditions for enrolment with other depts
				DCRSImpl obj=new DCRSImpl(dep.toUpperCase());
				bloop=(obj.dropcourseinowndept(studentId,courseId));

			}
			else if(data(buffer).toString().split(":")[0].equalsIgnoreCase("oldcourse")){

				String studentId=(data(buffer).toString().split(":")[1]);
				String courseId=(data(buffer).toString().split(":")[2]);
				bloop=(DCRSImpl.checkifoldcourseforowndept(studentId,courseId,dep.toUpperCase()));

			}
			else if(data(buffer).toString().split(":")[0].equalsIgnoreCase("availablespace")){

				String studentId=(data(buffer).toString().split(":")[1]);
				String courseId=(data(buffer).toString().split(":")[2]);
				bloop=DCRSImpl.checkSpaceAvailability(courseId);

			}

		}
		return bloop;
	}


}
