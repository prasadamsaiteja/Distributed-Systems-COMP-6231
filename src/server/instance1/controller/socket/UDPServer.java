package server.instance1.controller.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.AbstractMap.SimpleEntry;

import server.instance1.controller.Helper;
import server.instance1.controller.corba.DCRS;
import server.instance1.data.Database;
import server.instance3.util.Utils;
import utils.Config;
import utils.Constants;
import utils.Logger;
import utils.UDPUtilities;

import java.util.ArrayList;
import java.util.HashMap;

public class UDPServer extends Thread {

	private DatagramSocket socket;
	
	public UDPServer(String serverName) throws SocketException {
		String department = Database.getInstance(serverName).department;
		socket = new DatagramSocket(Database.serverPorts.get(department));
	}
	
	public void run() {
				
		try {
			
			do{		
				byte[] receivedBuffer = new byte[1000];
				DatagramPacket requestPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
				socket.receive(requestPacket);
	            
	            byte[] responseBuffer = processRequest(requestPacket);
	            if(responseBuffer == null)
	            	continue; //will reply to Front end manually

	            DatagramPacket replyPacket = new DatagramPacket(responseBuffer, responseBuffer.length, requestPacket.getAddress(), requestPacket.getPort());
	            socket.send(replyPacket);
	    
			} while(true);

		} catch (IOException exception) { exception.printStackTrace(); }
		finally {
			if (socket != null)
				socket.close();
		}
				
	}
	
	private byte[] processRequest(DatagramPacket requestPacket) {
		
		String receivedRequest = new String(requestPacket.getData(), 0, requestPacket.getLength());
		
		if(receivedRequest.startsWith("SEQUENCER&")) { 
			processSequencerRequest(receivedRequest.replace("SEQUENCER&", ""));
			return null;
		}
		
		else 
			return processUDPRequest(requestPacket.getData());
		
	}
	
	private byte[] processUDPRequest(byte[] requestByte) {		
		
		try{		
		
			String receivedRequest = new String(requestByte, 0, requestByte.length);
			String[] request = receivedRequest.split("&");
			
			for (int i = 0; i < request.length; i++) 
				request[i] = request[i].trim();
			
			switch(request[0]){
			
				case "ListCourseAvailability":
					HashMap<String, Integer> result = Helper.listCourseAvailability(request[1]);
					return UDPUtilities.objectToByteArray(result);
	
				case "EnrolCourse":
					SimpleEntry<Boolean, String> enrolCourseResult = Helper.enrolCourse(request[1], request[2], request[3], true);
					return UDPUtilities.objectToByteArray(enrolCourseResult);
					
				case "DropCourse":
					boolean dropCourseResult = Helper.dropCourse(request[1], request[2]);
					return UDPUtilities.objectToByteArray(dropCourseResult);
					
				case "ClassSchedule":
					HashMap<String, ArrayList<String>> classSchedule = Helper.getClassSchedule(request[1]);
					return UDPUtilities.objectToByteArray(classSchedule);
					
				case "GetCountOfEnrolledCourses":
					SimpleEntry<Integer, Integer> getCountOfEnrolledCoursesResult = Helper.getCountOfEnrolledCourses(request[1], request[2]);
					return UDPUtilities.objectToByteArray(getCountOfEnrolledCoursesResult);
					
				default:{
					if(Utils.byteArrayToObject(requestByte) instanceof HashMap) {
						HashMap<String, Object> temp = (HashMap<String, Object>) Utils.byteArrayToObject(requestByte);
						
						for (String key : temp.keySet()) {
							
							switch(key) {
							
								case Constants.OP_GETSTATE:
									Logger.log("A replica manager request instance copy");
									return Helper.deepCopyInstance1State();
									
								case Constants.OP_SETSTATE:	
									Helper.setState(temp.get(key));
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

		} catch(Exception exception) { exception.printStackTrace(); }
		
		return null;
	}
	
	private void processSequencerRequest(String receivedRequest) {
		
		String[] temp = receivedRequest.split("&", 2);
		//int sequenceNumber = Integer.parseInt(temp[0]);
		String request[] = temp[1].trim().split("&");
		byte[] response = new byte[1000];
		
		switch(request[0]){
		
			case "addCourse":
				response = UDPUtilities.objectToByteArray(DCRS.addCourse(request[1], request[2], request[3], Integer.parseInt(request[4])));
				break;
				
			case "removeCourse":
				response = UDPUtilities.objectToByteArray(DCRS.removeCourse(request[1], request[2], request[3]));
				break;
				
			case "listCourseAvailability":
				response = UDPUtilities.objectToByteArray(DCRS.listCourseAvailability(request[1], request[2]));
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