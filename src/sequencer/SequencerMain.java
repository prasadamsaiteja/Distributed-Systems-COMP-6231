package sequencer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import generic.Config;

public class SequencerMain extends Thread{
	
	private DatagramSocket datagramSocket;

	public void run(){
		
		byte recieveRequestArray[] = new byte[1000];
		System.out.println("Sequencer Initated");
		
		try{
			
			datagramSocket = new DatagramSocket(Config.getConfig("SEQUENCER_PORT"));
		
			while(true)	{
	
				DatagramPacket request= new DatagramPacket(recieveRequestArray, recieveRequestArray.length);
				datagramSocket.receive(request);
				
				System.out.println("Request recieved");				
				SequencerUDPHandlerThread newThread = new SequencerUDPHandlerThread(request);
				newThread.start();
				
			}
				
		} catch(Exception e) {
				e.printStackTrace();
		}
	}
		
	public static void main(String args[]){
		
		SequencerMain _startingObject= new SequencerMain();
		_startingObject.start();
		
	}

}