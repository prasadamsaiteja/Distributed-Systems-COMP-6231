package server.instance2.dcrsimpl;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import utils.Config;


public class UDPClient {

	
	static HashMap<String,Integer> serverRepo;
	static public HashMap<String,HashMap<String,HashMap<String,String>>> recordDetails;
	private static StringBuilder data(byte[] a) {
		// TODO Auto-generated method stub


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
	
static byte[] sendRequest(String action, int portNumber) {
		
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
	
	public  String udpRequest(String dept,String dummy) {
		serverRepo=new HashMap<>();
		try {
		serverRepo.put("COMP", Config.getConfig("INSTANCE2_COMP_PORT"));
		serverRepo.put("INSE",Config.getConfig("INSTANCE2_INSE_PORT"));
		serverRepo.put("SOEN",Config.getConfig("INSTANCE2_SOEN_PORT"));	
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		DatagramSocket ds = null;
		DatagramPacket request1=null;
		DatagramPacket reply1=null;
		String response1=new String();
		try {
			HashMap<String, Integer> availableCourseList=new HashMap();

			byte[] message = dummy.getBytes();

			byte[] buffer2 = new byte[1000];

			InetAddress aHost = InetAddress.getByName("127.0.0.1");

			System.out.println("coming here 1");

			System.out.println("In UDPRequest:"+dummy);
			ds = new DatagramSocket();
			if(dept.contains("COMP"))
				request1 = new DatagramPacket(message, message.length, aHost, serverRepo.get(dept));
			else if(dept.contains("SOEN"))
				request1 = new DatagramPacket(message, message.length, aHost, serverRepo.get(dept));
			else if(dept.contains("INSE"))
				request1 = new DatagramPacket(message, message.length, aHost, serverRepo.get(dept));


			ds.send(request1);
			System.out.println(new String(request1.getData()));

			reply1 = new DatagramPacket(buffer2, buffer2.length);
			ds.receive(reply1);
			System.out.println(new String(reply1.getData()));
			response1= new String(reply1.getData());
			response1=response1.trim();
			
			ds.close();
		}
		catch(SocketException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			if(ds!=null)
				ds.close();
		}

		return response1;
	}

}
