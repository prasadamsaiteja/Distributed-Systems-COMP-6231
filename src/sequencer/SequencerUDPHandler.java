package sequencer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import utils.Config;

public class SequencerUDPHandler {
	
	private String request;	
	private String serverName;
	
	public SequencerUDPHandler(int sequenceNumber, String requestMessage) {
		this.request = requestMessage;
		
		String[] requestParts = request.split("&", 2);
		this.serverName = requestParts[0];
		this.request = "SEQUENCER&" + sequenceNumber + "&" + requestParts[1];
	}
	
	public void sendToReplica() {

		try {
			
			byte[] buffer = request.getBytes();
			
			DatagramPacket datagramPacket1 = new DatagramPacket(buffer, buffer.length, instanceInetAddress(1), getServerPortNumber(1));
			DatagramPacket datagramPacket2 = new DatagramPacket(buffer, buffer.length, instanceInetAddress(2), getServerPortNumber(2));
			DatagramPacket datagramPacket3 = new DatagramPacket(buffer, buffer.length, instanceInetAddress(3), getServerPortNumber(3));
			DatagramPacket datagramPacket4 = new DatagramPacket(buffer, buffer.length, instanceInetAddress(4), getServerPortNumber(4));
			
			DatagramSocket socket= new DatagramSocket();
			socket.send(datagramPacket1);
			socket.send(datagramPacket2);
			socket.send(datagramPacket3);
			socket.send(datagramPacket4);
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public InetAddress instanceInetAddress(int instance) throws UnknownHostException, FileNotFoundException, IOException {
		return InetAddress.getByName(Config.getStringConfig("INSTANCE" + instance + "_IP"));
	}
	
	public int getServerPortNumber(int instance) throws FileNotFoundException, IOException {
		return Config.getConfig("INSTANCE" + instance + "_" + serverName + "_PORT");
	}
	
}