package server.instance2.Impl;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import generic.Config;
import generic.UDPUtilities;




public class UDPServerThreads extends Thread{


	static HashMap<String,Integer> serverRepo;
	String dept;

	public UDPServerThreads(String dept) throws FileNotFoundException, IOException {
		super();
		this.dept = dept;
		serverRepo=new HashMap<>();
		serverRepo.put("COMP", Config.getConfig("INSTANCE2_COMP_PORT"));
		serverRepo.put("INSE",Config.getConfig("INSTANCE2_INSE_PORT"));
		serverRepo.put("SOEN",Config.getConfig("INSTANCE2_SOEN_PORT"));			
		
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

	Impl compServer,inseServer;
	Impl soenServer;
	static String department;

	public  void run() {
		DatagramSocket COMP = null;
		try {
			department = dept.trim();
			System.out.println(department+" started UDPServer");
			if(dept.contains("COMP")) {
				COMP = new DatagramSocket(serverRepo.get(dept));
			//	compServer=new Impl(dept);

				recordDetails=Impl.recordDetails;
			}
			else if(dept.contains("INSE")) {
				COMP = new DatagramSocket(serverRepo.get(dept));
				//inseServer=new Impl(dept);

				recordDetails=Impl.recordDetails;
			}
			else if(dept.contains("SOEN")) {
				COMP = new DatagramSocket(serverRepo.get(dept));
			//	soenServer=new Impl(dept);				
				recordDetails=Impl.recordDetails;
			}
			while(true){

				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				COMP.receive(request);
				String data = new String(request.getData(), 0, request.getLength());
				System.out.println("Whats coming here:"+data);
				byte[] blah = processRequest(data, COMP,dept,recordDetails);
				if (blah == null)
					continue; // will reply to Front end manually
				//String bloop=gettheData(buffer,dept,recordDetails);
				//byte[] blah = bloop.getBytes();
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

	private byte[] processRequest(String data, DatagramSocket socket,String dept,HashMap<String,HashMap<String,HashMap<String,Object>>> recordDetails) throws IOException {
		System.out.println("Is it reaching here?");
		if (data.startsWith("SEQUENCER&")) {
			processSequencerRequest(data.replace("SEQUENCER&", ""), socket);
			return null;
		}

		else
			return gettheData(data,dept,recordDetails);
	}

	private void processSequencerRequest(String receivedRequest, DatagramSocket socket) {

		String[] temp = receivedRequest.split("&", 2);
		
		String request[] = temp[1].split("&");
		byte[] response = new byte[1000];
		try {
			switch (request[0]) {

			case "addCourse":
				Impl newObj= new Impl(department);

				response = UDPUtilities
						.objectToByteArray(newObj.addCourse(request[1], request[2], request[3], Integer.parseInt(request[4])));

				break;

			case "removeCourse":
				Impl newObj1= new Impl(department);
				response = UDPUtilities.objectToByteArray(newObj1.removeCourse(request[1], request[2], request[3]));
				break;

			case "listCourseAvailability":
				Impl newObj2= new Impl(department);
				response = UDPUtilities.objectToByteArray(newObj2.listCourseAvailability(request[1], request[2]));
				break;

			case "enrolCourse":
				Impl newObj3= new Impl(department);
				response = UDPUtilities.objectToByteArray(newObj3.enrolCourse(request[1], request[2], request[3]));
				break;

			case "getClassSchedule":
				Impl newObj4= new Impl(department);
				response = UDPUtilities.objectToByteArray(newObj4.getClassSchedule(request[1]));
				break;

			case "dropCourse":
				Impl newObj5= new Impl(department);
				response = UDPUtilities.objectToByteArray(newObj5.dropCourse(request[1], request[2]));
				break;

			case "swapCourse":
				Impl newObj6= new Impl(department);
				response = UDPUtilities.objectToByteArray(newObj6.swapCourse(request[1], request[2], request[3]));
				break;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			InetAddress frontendIP = InetAddress.getByName(Config.getStringConfig("FRONTEND_IP"));
			DatagramPacket replyPacket = new DatagramPacket(response, response.length, frontendIP,
					Config.getConfig("FRONTEND_PORT"));
			socket.send(replyPacket);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private byte[] gettheData(String buffer,String dep, HashMap<String, HashMap<String, HashMap<String, Object>>> recordDetails2) throws IOException {
		// TODO Auto-generated method stub
		String bloop=new String();
		
		System.out.println("Data Fetched::"+buffer);
		if(buffer != null && buffer != null && !buffer.isEmpty() )
		{
			if(buffer.split(":")[0].equalsIgnoreCase("listcourse")){
				String semester= (buffer.split(":")[1]);
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
			else if(buffer.split(":")[0].equalsIgnoreCase("enrolcourse")){
				String sem=(buffer.split(":")[1]);
				String studentId=(buffer.split(":")[2]);
				String courseId=(buffer.split(":")[3]);
				//write a method to check conditions for enrolment with other depts
				Impl obj=new Impl(dep.toUpperCase());
				
				bloop=obj.enrolCourseinowndept(studentId, courseId, sem);

			}
			else if(buffer.split(":")[0].equalsIgnoreCase("semcount")){
				
				String sem=(buffer.split(":")[1]);
				String studentId=(buffer.split(":")[2]);
								
				Impl obj=new Impl(dep.toUpperCase());
				
				bloop=String.valueOf(obj.returnDeptSemesterCountForStudent(studentId, sem));
				System.out.println("sem count:" +bloop);
			}
			else if(buffer.split(":")[0].equalsIgnoreCase("classschedule")){

				String studentId=(buffer.split(":")[1]);

				//write a method to check conditions for enrolment with other depts
				bloop=Impl.getOwnClassSchedule(studentId);

			}
			else if(buffer.split(":")[0].equalsIgnoreCase("getOtherCourseCount")){

				String studentId=(buffer.split(":")[1]);
				
				bloop=String.valueOf(Impl.returnOtherdepartmentCount(studentId,dep));
				System.out.println(bloop);
			}
			else if(buffer.split(":")[0].equalsIgnoreCase("dropcourse")){

				String studentId=(buffer.split(":")[1]);
				String courseId=(buffer.split(":")[2]);
				
				//write a method to check conditions for enrolment with other depts
				Impl obj=new Impl(dep.toUpperCase());
				bloop=(obj.dropcourseinowndept(studentId,courseId));

			}
			else if(buffer.split(":")[0].equalsIgnoreCase("oldcourse")){

				String studentId=(buffer.split(":")[1]);
				String courseId=(buffer.split(":")[2]);
				bloop=(Impl.checkifoldcourseforowndept(studentId,courseId,dep.toUpperCase()));

			}
			else if(buffer.split(":")[0].equalsIgnoreCase("availablespace")){

				String studentId=(buffer.split(":")[1]);
				String courseId=(buffer.split(":")[2]);
				bloop=Impl.checkSpaceAvailability(courseId);

			}

		}
		return UDPUtilities.objectToByteArray(bloop);
	}


}
