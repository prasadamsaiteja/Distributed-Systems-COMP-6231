package frontEnd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import generic.Config;
import generic.UDPUtilities;

public class FrontEndUtitlies {
	
	public static Object sendUDPRequest(String message) {
		
		try {
			byte[] buffer = message.getBytes();
			
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(500);
			
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), Config.getConfig("SEQUENCER_PORT"));
			socket.send(requestPacket);		
		
		} catch(Exception ignored){}

		try{
			
			byte[] receivedBuffer = new byte[46595];
			DatagramPacket replyPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
			FrontEnd.datagramSocket.receive(replyPacket);		
			return UDPUtilities.byteArrayToObject(replyPacket.getData());
			
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;	
		}
			
		
	}
	
}