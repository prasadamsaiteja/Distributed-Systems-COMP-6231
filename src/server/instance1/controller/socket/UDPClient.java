package server.instance1.controller.socket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import server.instance1.controller.Helper;
import server.instance1.data.Database;
import utils.UDPUtilities;

public class UDPClient {

	private static byte[] sendRequest(String action, int portNumber) {
		
		try {
			byte[] buffer = action.getBytes();
			
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(500);
			
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), portNumber);
			socket.send(requestPacket);
			
			byte[] receivedBuffer = new byte[46595];
			DatagramPacket replyPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
			socket.receive(replyPacket);			
			socket.close();
			
			return replyPacket.getData();
		
		} catch(Exception ignored){}
		
		return null;
	}
	
	public static HashMap<String, Integer> getListCourseAvailability(String semester) {
		
		HashMap<String, Integer> response = new HashMap<>();
		
		try{
			
			for (Entry<String, Integer> entry : Database.serverPorts.entrySet()) {
				
				if(entry.getKey().equals(Database.getInstance().department))
					continue;
				
				byte[] result = sendRequest("ListCourseAvailability&" + semester, entry.getValue());
				
				HashMap<String, Integer> temp = (HashMap<String, Integer>) UDPUtilities.byteArrayToObject(result);
				response.putAll(temp);

			}
			
		} catch(Exception ignored){}
				
		return response;
	}
	
	public static SimpleEntry<Boolean, String> enrolCourse(String studentID, String courseId, String semester) {
		
		SimpleEntry<Boolean, String> result = new SimpleEntry<>(false, "course_doesnt_exist");
		
		try{
			
			for (Entry<String, Integer> entry : Database.serverPorts.entrySet()) {
				
				if(!courseId.startsWith(entry.getKey()))
					continue;
								
				byte[] response = sendRequest("EnrolCourse&" + studentID + "&" + courseId + "&" + semester, entry.getValue());
				result = (SimpleEntry<Boolean, String>) UDPUtilities.byteArrayToObject(response);

			}
			
		} catch(Exception exception){
			exception.printStackTrace();
		}
				
		return result;
		
	}
	
	public static boolean dropCourse(String studentId, String courseId) {
		
		boolean result = false;
		
		try{
			
			for (Entry<String, Integer> entry : Database.serverPorts.entrySet()) {
				
				if(!courseId.startsWith(entry.getKey()))
					continue;

				byte[] response = sendRequest("DropCourse&" + studentId + "&" + courseId, entry.getValue());				
				result = (boolean) UDPUtilities.byteArrayToObject(response);

			}
			
		} catch(Exception ignored){}
		
		return result;
	}
	
	public static HashMap<String, ArrayList<String>> getClassSchedule(String studentId) {

		HashMap<String, ArrayList<String>> result = new HashMap<>();
		
		try{
			
			for (Entry<String, Integer> entry : Database.serverPorts.entrySet()) {
				
				HashMap<String, ArrayList<String>> temp;
				
				if(entry.getKey().equals(Database.getInstance().department))
					temp = Helper.getClassSchedule(studentId);
				
				else{								
					byte[] response = sendRequest("ClassSchedule&" + studentId, entry.getValue());				
					temp = (HashMap<String, ArrayList<String>>) UDPUtilities.byteArrayToObject(response);
				}
				
				for (Entry<String, ArrayList<String>> temp2: temp.entrySet()) {
					if(!result.containsKey(temp2.getKey()))
						result.put(temp2.getKey(), temp2.getValue());

					else{
						ArrayList<String> temp2Result = result.get(temp2.getKey());
						temp2Result.addAll(temp2.getValue());
						result.put(temp2.getKey(), temp2Result);
					}
				}
				
			}
			
		} catch(Exception ignored){}
				
		return result;
	}
	
	public static SimpleEntry<Integer, Integer> getCountOfEnrolledCourses(String studentId, String semester) {
		
		SimpleEntry<Integer, Integer> result = new SimpleEntry<Integer, Integer>(0, 0);
		
		try{
			
			for (Entry<String, Integer> entry : Database.serverPorts.entrySet()) {
				
				SimpleEntry<Integer, Integer> temp;
				
				if(entry.getKey().equals(Database.getInstance().department))
					temp = Helper.getCountOfEnrolledCourses(studentId, semester);
					
				else {
					byte[] response = sendRequest("GetCountOfEnrolledCourses&" + studentId + "&" + semester, entry.getValue());				
					temp = (SimpleEntry<Integer, Integer>) UDPUtilities.byteArrayToObject(response);
				}
				
				result = new SimpleEntry<Integer, Integer>(result.getKey() + temp.getKey(), result.getValue() + temp.getValue());
			}
			
		} catch(Exception ignored){}
		
		return result;		
	}
	
}