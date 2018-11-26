package frontEnd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import generic.Config;

public class FrontEndUtitlies {
	
	public static void sendUDPRequest(String message){
		
		try {
			byte[] buffer = message.getBytes();
			
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(500);
			
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), Config.getConfig("SEQUENCER_PORT"));
			socket.send(requestPacket);		
		
		} catch(Exception ignored){}
			
	}
	
}