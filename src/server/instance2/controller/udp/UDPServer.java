package server.instance2.controller.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import server.instance1.controller.Helper;
import server.instance2.controller.DCRS;
import server.instance3.util.Utils;
import utils.Config;
import utils.Constants;
import utils.Logger;
import utils.UDPUtilities;

public class UDPServer extends Thread {

	private Integer SOENPort = Config.getConfig("INSTANCE2_SOEN_PORT");
	private Integer INSEPort = Config.getConfig("INSTANCE2_INSE_PORT");
	private Integer COMPPort = Config.getConfig("INSTANCE2_COMP_PORT");

	private HashMap<String, HashMap<String, HashMap<String, Object>>> recordDetails;
	private HashMap<String, Integer> serverRepo;
	private DCRS dcrs;
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
				dcrs = new DCRS(dept);
				recordDetails = dcrs.getCourseRecords();
			} else if (dept.contains("INSE")) {
				socket = new DatagramSocket(INSEPort);
				dcrs = new DCRS(dept);
				recordDetails = dcrs.getCourseRecords();
			} else if (dept.contains("SOEN")) {
				socket = new DatagramSocket(SOENPort);
				dcrs = new DCRS(dept);
				recordDetails = dcrs.getCourseRecords();
			}
			while (true) {

				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				byte[] bloop = gettheData(buffer, dept, recordDetails);
				
				if(bloop == null)
					continue;
				
				DatagramPacket reply = new DatagramPacket(bloop, bloop.length, request.getAddress(), request.getPort());
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

	private byte[] gettheData(byte[] buffer, String dep, HashMap<String, HashMap<String, HashMap<String, Object>>> recordDetails2) throws IOException {
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
				bloop = dcrs.getOwnClassSchedule(studentId);

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("getOtherCourseCount")) {

				String studentId = (data(buffer).toString().split(":")[1]);

				bloop = String.valueOf(dcrs.returnOtherdepartmentCount(studentId, dep));

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
				bloop = (dcrs.checkifoldcourseforowndept(studentId, courseId, dep.toUpperCase()));

			} else if (data(buffer).toString().split(":")[0].equalsIgnoreCase("availablespace")) {

				String studentId = (data(buffer).toString().split(":")[1]);
				String courseId = (data(buffer).toString().split(":")[2]);
				bloop = dcrs.checkSpaceAvailability(courseId);

			} else {

				if(Utils.byteArrayToObject(buffer) instanceof HashMap) {
					
					HashMap<String, Object> temp = (HashMap<String, Object>) Utils.byteArrayToObject(buffer);
					
					for (String key : temp.keySet()) {
						
						switch(key) {
						
							case Constants.OP_GETSTATE:
								Logger.log("A replica manager request instance copy");
								return dcrs.deepCopyInstance2State();
								
							case Constants.OP_SETSTATE:	
								dcrs.setState(temp.get(key));
								return String.valueOf(true).getBytes();
								
							case Constants.OP_ISALIVE:
								Logger.log("Received Is Alive Request");
								return String.valueOf(true).getBytes();
						}
					}
				}
				
				return null;
			}

		}
		return bloop.getBytes();
	}
	
	private void processSequencerRequest(String receivedRequest) {
		
		String[] temp = receivedRequest.split("&", 2);
		//int sequenceNumber = Integer.parseInt(temp[0]);
		String request[] = temp[1].trim().split("&");
		byte[] response = new byte[1000];
		
		switch(request[0]){
		
			case "addCourse":
				response = UDPUtilities.objectToByteArray(dcrs.addCourse(request[2], request[3], request[4]));
				break;
				
			case "removeCourse":
				response = UDPUtilities.objectToByteArray(dcrs.removeCourse(request[2], request[3]));
				break;
				
			case "listCourseAvailability":
				response = UDPUtilities.objectToByteArray(dcrs.listCourseAvailability(request[2]));
				break;
				
			case "enrolCourse":
				response = UDPUtilities.objectToByteArray(dcrs.enrolCourse(request[1], request[2], request[3]));
				break;
				
			case "getClassSchedule":
				response = UDPUtilities.objectToByteArray(dcrs.getClassSchedule(request[1]));
				break;
				
			case "dropCourse":
				response = UDPUtilities.objectToByteArray(dcrs.dropCourse(request[1], request[2]));
				break;
				
			case "swapCourse":
				response = UDPUtilities.objectToByteArray(dcrs.swapCourse(request[1], request[2], request[3]));
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
