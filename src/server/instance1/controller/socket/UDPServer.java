package server.instance1.controller.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.AbstractMap.SimpleEntry;

import generic.UDPUtilities;
import generic.Config;
import server.instance1.controller.Helper;
import server.instance1.controller.rmi.DCRS;
import server.instance1.data.Database;


import java.util.ArrayList;
import java.util.HashMap;

public class UDPServer extends Thread {

	private DatagramSocket socket;
	
	public UDPServer() throws SocketException{
		String department = Database.getInstance().department;
		socket = new DatagramSocket(Database.serverPorts.get(department));
	}
	
	public void run() {
		
		do{
			try {
				
				byte[] receivedBuffer = new byte[1000];
				DatagramPacket requestPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
				socket.receive(requestPacket);

	            String request = new String(requestPacket.getData(), 0, requestPacket.getLength());      
	            byte[] responseBuffer = processRequest(request);
	            if(responseBuffer == null)
	            	continue; //will reply to Front end manually

	            DatagramPacket replyPacket = new DatagramPacket(responseBuffer, responseBuffer.length, requestPacket.getAddress(), requestPacket.getPort());
	            socket.send(replyPacket);
	            
			} catch (IOException exception) { exception.printStackTrace(); }
			
		} while(true);
		
	}
	
	private byte[] processRequest(String receivedRequest) {
		
		if(receivedRequest.startsWith("SEQUENCER&")) {
			processSequencerRequest(receivedRequest.replace("SEQUENCER&", ""));
			return null;
		}
		
		else 
			return processUDPRequest(receivedRequest);
		
	}
	
	private byte[] processUDPRequest(String receivedRequest) {
		
		try{
		
			String[] request = receivedRequest.split("&");
			
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
					
			}

		} catch(Exception exception) { exception.printStackTrace(); }
		
		return null;
	}
	
	private void processSequencerRequest(String receivedRequest) {
		
		String[] temp = receivedRequest.split("&", 2);
		//int sequenceNumber = Integer.parseInt(temp[0]);
		String request[] = temp[1].split("&");
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
			
			InetAddress serverIP = InetAddress.getByName(Config.getStringConfig("FRONTEND_IP"));
			DatagramPacket replyPacket = new DatagramPacket(response, response.length, serverIP, Config.getConfig("FRONTEND_PORT"));
	        socket.send(replyPacket);
	        
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
}