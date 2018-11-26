package sequencer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import generic.Config;

public class SequencerUDPHandler {
	
	private String request;	
	private String serverName;
	
	public SequencerUDPHandler(int sequenceNumber, String requestMessage) {
		this.request = requestMessage;
		
		String[] requestParts = request.split("&", 2);
		this.serverName = requestParts[0];
		this.request = "SEQUENCER&" + sequenceNumber + "&" + this.request;
	}
	
	public void sendToReplica() {

		try {
			
			byte[] buffer = request.getBytes();
			
			DatagramPacket datagramPacket1 = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), getServerPortNumber(1));
			DatagramPacket datagramPacket2 = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), getServerPortNumber(2));
			DatagramPacket datagramPacket3 = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), getServerPortNumber(3));
			
			DatagramSocket socket= new DatagramSocket();
			socket.send(datagramPacket1);
			socket.send(datagramPacket2);
			socket.send(datagramPacket3);
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public int getServerPortNumber(int instance) throws FileNotFoundException, IOException {
		return Config.getConfig("INSTANCE" + instance + "_" + serverName + "_PORT");
	}
	
}