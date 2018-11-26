package server.instance1.controller.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.AbstractMap.SimpleEntry;

import server.instance1.controller.Helper;
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

	            DatagramPacket replyPacket = new DatagramPacket(responseBuffer, responseBuffer.length, requestPacket.getAddress(), requestPacket.getPort());
	            socket.send(replyPacket);
	            
			} catch (IOException exception) { exception.printStackTrace(); }
			
		} while(true);
		
	}
	
	public static byte[] objectToByteArray(Object object) {		

		try {
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(object);
			return byteOut.toByteArray();			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new byte[1024];
	}
	
	private byte[] processRequest(String receivedRequest) {
		
		try{
		
			String[] request = receivedRequest.split("&");
			
			switch(request[0]){
			
				case "ListCourseAvailability":
					HashMap<String, Integer> result = Helper.listCourseAvailability(request[1]);
					return objectToByteArray(result);
	
				case "EnrolCourse":
					SimpleEntry<Boolean, String> enrolCourseResult = Helper.enrolCourse(request[1], request[2], request[3], true);
					return objectToByteArray(enrolCourseResult);
					
				case "DropCourse":
					boolean dropCourseResult = Helper.dropCourse(request[1], request[2]);
					return objectToByteArray(dropCourseResult);
					
				case "ClassSchedule":
					HashMap<String, ArrayList<String>> classSchedule = Helper.getClassSchedule(request[1]);
					return objectToByteArray(classSchedule);
					
				case "GetCountOfEnrolledCourses":
					SimpleEntry<Integer, Integer> getCountOfEnrolledCoursesResult = Helper.getCountOfEnrolledCourses(request[1], request[2]);
					return objectToByteArray(getCountOfEnrolledCoursesResult);
					
			}

		} catch(Exception exception) { exception.printStackTrace(); }
		
		return null;
	}
	
}