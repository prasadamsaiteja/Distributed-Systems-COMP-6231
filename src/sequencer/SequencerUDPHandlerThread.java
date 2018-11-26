package sequencer;

import java.awt.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SequencerUDPHandlerThread extends Thread {
	
	private DatagramPacket request;
	private ByteArrayInputStream bais;
	private ObjectInputStream ois;
	private byte array[];
	private ByteArrayOutputStream baos; 
	private ObjectOutputStream oos;
	
	public SequencerUDPHandlerThread(DatagramPacket request) {
		this.request = request;
	}
	
	@Override
	public void run() {
		
		try {
			
			ip= InetAddress.getByName("localhost");
		
			baos= new ByteArrayOutputStream();
			oos= new ObjectOutputStream(baos);
			bais = new ByteArrayInputStream(request.getData());
			ois= new ObjectInputStream(bais);
			Object recievedObject=ois.readObject();
			String request = recievedObject.toString();
			//String temp[];
			if(request != null) {
				String temp[] = request.split("&");
				
				String dep =temp[0];
				
				String oprn = temp [1];				
				
				switch(oprn) {
					case "ADD_COURSE" :
						synchronized (object._sequenceNumber) {
							request = request+ "&" +(object._sequenceNumber++);
						}					
						byte[] b1 = request.getBytes();
						sendToReplica(b1);
						break;
					case "DROP_COURSE" :
						synchronized (object._sequenceNumber) {
							request = request+ "&" +(object._sequenceNumber++);
						}					
						byte[] b2 = request.getBytes();
						sendToReplica(b2);
						break;
					case "REMOVE_COURSE" :
						synchronized (object._sequenceNumber) {
							request = request+ "&" +(object._sequenceNumber++);
						}					
						byte[] b3 = request.getBytes();
						sendToReplica(b3);
						break;
					case "SWAP_COURSE" :
						synchronized (object._sequenceNumber) {
							request = request+ "&" +(object._sequenceNumber++);
						}					
						byte[] b4 = request.getBytes();
						sendToReplica(b4);
						break;
					case "LIST_COURSE" :
						synchronized (object._sequenceNumber) {
							request = request+ "&" +(object._sequenceNumber++);
						}					
						byte[] b5 = request.getBytes();
						sendToReplica(b5);
						break;
					case "ENROL_COURSE" :
						synchronized (object._sequenceNumber) {
							request = request+ "&" +(object._sequenceNumber++);
						}					
						byte[] b6 = request.getBytes();
						sendToReplica(b6);
						break;
					case "CLASS_SCHEDULE" :
						synchronized (object._sequenceNumber) {
							request = request+ "&" +(object._sequenceNumber++);
						}					
						byte[] b7 = request.getBytes();
						sendToReplica(b7);
						break;
				}
				
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		

	}
	public void sendToReplica(byte array[])
	{

		try {
			
			DatagramPacket packet1= new DatagramPacket(array,array.length,ip,object.portNumberOfReplica1);
			DatagramPacket packet2= new DatagramPacket(array,array.length,ip,object.portNumberOfReplica2);
			DatagramPacket packet3= new DatagramPacket(array,array.length,ip,object.portNumberOfReplica3);
			DatagramPacket packet4= new DatagramPacket(array,array.length,ip,object.portNumberOfReplica4);
			DatagramSocket socket= new DatagramSocket();
			
			socket.send(packet1);
			socket.send(packet2);
			socket.send(packet3);
			socket.send(packet4);
			System.out.println ( "Sent : " + object._sequenceNumber) ;
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
	

