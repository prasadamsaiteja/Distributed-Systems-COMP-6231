package sequencer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import utils.Config;

public class Sequencer extends Thread {
	
	private DatagramSocket datagramSocket;
	int sequenceNumber = 0;

	public void run() {
		
		byte buffer[] = new byte[1000];
		System.out.println("Sequencer initated");
		
		try{
			
			datagramSocket = new DatagramSocket(Config.getConfig("SEQUENCER_PORT"));
		
			while(true)	{
	
				DatagramPacket requestPacket= new DatagramPacket(buffer, buffer.length);
				datagramSocket.receive(requestPacket);				
				System.out.println("Request recieved");
			
				String requestMessage = new String(requestPacket.getData(), 0, requestPacket.getLength());
				SequencerUDPHandler sequencerUDPHandler = new SequencerUDPHandler(sequenceNumber++, requestMessage);
				sequencerUDPHandler.sendToReplica();
				System.out.println("Request broadcasted with sequence number: " + (sequenceNumber - 1) + "\n");
				
			}
				
		} catch(Exception e) {
			e.printStackTrace();
		} finally{
			if(datagramSocket != null) datagramSocket.close();
		}
	}
		
	public static void main(String args[]) {
		Sequencer sequencer = new Sequencer();
		sequencer.start();		
	}

}