package server.instance2.controller.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import server.instance2.controller.DCRS;
import generic.Config;
import generic.UDPUtilities;

public class UDPServer extends Thread {

	private Integer SOENPort = Config.getConfig("INSTANCE2_SOEN_PORT");
	private Integer INSEPort = Config.getConfig("INSTANCE2_INSE_PORT");
	private Integer COMPPort = Config.getConfig("INSTANCE2_COMP_PORT");

	private static HashMap<String, HashMap<String, HashMap<String, Object>>> recordDetails;
	private static HashMap<String, Integer> serverRepo;
	private DCRS compServer, inseServer, soenServer;
	private String dept;
	private DatagramSocket socket = null;
	
	public UDPServer(String dept) {
		super();
		this.dept = dept;
		serverRepo = new HashMap<>();
		serverRepo.put("COMP", COMPPort);
		serverRepo.put("INSE", INSEPort);
		serverRepo.put("SOEN", SOENPort);
	}

	private static String data(byte[] a) {
		return new String(a, 0, a.length);
	}
	
	@Override
	public void run()  {
		
		try {
			if (dept.contains("COMP")) {
				socket = new DatagramSocket(COMPPort);
				compServer = new DCRS(dept);
				recordDetails = compServer.getCourseRecords();
			} else if (dept.contains("INSE")) {
				socket = new DatagramSocket(INSEPort);
				inseServer = new DCRS(dept);

				recordDetails = inseServer.getCourseRecords();
			} else if (dept.contains("SOEN")) {
				socket = new DatagramSocket(SOENPort);
				soenServer = new DCRS(dept);
				recordDetails = soenServer.getCourseRecords();
			}
			while (true) {

				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				String bloop = gettheData(buffer, dept, recordDetails);
				
				if(bloop == null)
					continue;
				
				byte[] blah = bloop.getBytes();
				DatagramPacket reply = new DatagramPacket(blah, blah.length, request.getAddress(), request.getPort());
				socket.send(reply);

			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}

	private String gettheData(byte[] buffer, String dep, HashMap<String, HashMap<String, HashMap<String, Object>>> recordDetails2) throws IOException {
		// TODO Auto-generated method stub
		String bloop = new String();
		
		if(data(buffer).toString().startsWith("SEQUENCER&")) {
			processSequencerRequest(data(buffer).toString().replace("SEQUENCER&", ""));
			return null;
		}
			
		if (buffer != null && data(buffer).toString() != null && !data(buffer).toString().isEmpty()) {
			if (data(buffer).toString().split(":")[0].equalsIgnoreCase("listcourse")) {
				String semester = (data(buffer).toString().split(":")[1]);
				if (recordDetails2 != null) {
					HashMap<String, HashMap<String, Object>> semCourseList = recordDetails2.get(semester);
					if (semCourseList != null) {

						for (Map.Entry<String, HashMap<String, Object>> map : semCourseList.entrySet()) {
							if (bloop.isEmpty()) {
								bloop = map.getKey() + "=" + map.getValue().get("capacity");
							} else {
								bloop = bloop + "," + map.getKey() + "=" + map.getValue().get("capacity");

							}
						}

					}
				}
			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("enrolcourse")) {
				String sem = (data(buffer).toString().split(":")[1]);
				String studentId = (data(buffer).toString().split(":")[2]);
				String courseId = (data(buffer).toString().split(":")[3]);
				// write a method to check conditions for enrolment with other
				// depts
				DCRS obj = new DCRS(dep.toUpperCase());

				bloop = obj.enrolCourseinowndept(studentId, courseId, sem);

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("semcount")) {
				String sem = (data(buffer).toString().split(":")[1]);
				String studentId = (data(buffer).toString().split(":")[2]);

				DCRS obj = new DCRS(dep.toUpperCase());

				bloop = String.valueOf(obj.returnDeptSemesterCountForStudent(studentId, sem));

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("classschedule")) {

				String studentId = (data(buffer).toString().split(":")[1]);

				// write a method to check conditions for enrolment with other
				// depts
				bloop = DCRS.getOwnClassSchedule(studentId);

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("getOtherCourseCount")) {

				String studentId = (data(buffer).toString().split(":")[1]);

				bloop = String.valueOf(DCRS.returnOtherdepartmentCount(studentId, dep));

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("dropcourse")) {

				String studentId = (data(buffer).toString().split(":")[1]);
				String courseId = (data(buffer).toString().split(":")[2]);

				// write a method to check conditions for enrolment with other
				// depts
				DCRS obj = new DCRS(dep.toUpperCase());
				bloop = (obj.dropcourseinowndept(studentId, courseId));

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("oldcourse")) {

				String studentId = (data(buffer).toString().split(":")[1]);
				String courseId = (data(buffer).toString().split(":")[2]);
				bloop = (DCRS.checkifoldcourseforowndept(studentId, courseId, dep.toUpperCase()));

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("availablespace")) {

				String studentId = (data(buffer).toString().split(":")[1]);
				String courseId = (data(buffer).toString().split(":")[2]);
				bloop = DCRS.checkSpaceAvailability(courseId);

			}

		}
		return bloop;
	}
	
	private void processSequencerRequest(String receivedRequest) {
		
		String[] temp = receivedRequest.split("&", 2);
		//int sequenceNumber = Integer.parseInt(temp[0]);
		String request[] = temp[1].trim().split("&");
		byte[] response = new byte[1000];
		
		switch(request[0]){
		
			case "addCourse":
				response = UDPUtilities.objectToByteArray(DCRS.addCourse(request[1], request[2], request[3]));
				break;
				
			case "removeCourse":
				response = UDPUtilities.objectToByteArray(DCRS.removeCourse(request[1], request[2]));
				break;
				
			case "listCourseAvailability":
				response = UDPUtilities.objectToByteArray(DCRS.listCourseAvailability(request[1]));
				break;
				
			case "enrolCourse":
				response = UDPUtilities.objectToByteArray(DCRS.enrolCourse(request[1], request[2], request[3]));
				break;
				
			case "getClassSchedule":
				response = UDPUtilities.objectToByteArray(DCRS.getClassSchedule(request[1]));
				break;
				
			case "dropCourse":
				response = UDPUtilities.objectToByteArray(DCRS.dropCourse(request[1], request[2]));
				break;
				
			case "swapCourse":
				response = UDPUtilities.objectToByteArray(DCRS.swapCourse(request[1], request[2], request[3]));
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
	
}
