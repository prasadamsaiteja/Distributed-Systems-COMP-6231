package frontEnd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import generic.Config;
import generic.UDPUtilities;

public class FrontEndUtitlies {
	
	public static Object sendUDPRequest(String message) {
		
		try {
			byte[] buffer = message.getBytes();
			
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(500);
			
			InetAddress sequencerIP = InetAddress.getByName(Config.getStringConfig("SEQUENCER_IP"));
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, sequencerIP, Config.getConfig("SEQUENCER_PORT"));
			socket.send(requestPacket);		
			System.out.println("Request sent to sequencer");
			
		} catch(Exception ex){
			ex.printStackTrace();
		}

		try{
			
			/*ExecutorService executor = Executors.newSingleThreadExecutor();

			@SuppressWarnings("unchecked")
			Future<Object> handler = executor.submit(new Callable() {
			    @Override
			    public Object call() throws IOException {
			        
			    	int replyReceived = 0;
			    	Object instance1 = null, instance2 = null, instance3 = null;
			    	
			    	while(replyReceived < 1) {
						
						DatagramPacket replyPacket = receiveReply();
						
						if(replyPacket.getPort() == getPort(1, "COMP") || replyPacket.getPort() == getPort(1, "SOEN") || replyPacket.getPort() == getPort(1, "INSE")) {
							instance1 = UDPUtilities.byteArrayToObject(replyPacket.getData());
							System.out.println("Reply received from instance 1");
							replyReceived++;
						}
											
						else if(replyPacket.getPort() == getPort(2, "COMP") || replyPacket.getPort() == getPort(2, "SOEN") || replyPacket.getPort() == getPort(2, "INSE")) {
							instance2 = UDPUtilities.byteArrayToObject(replyPacket.getData());
							System.out.println("Reply received from instance 2");
							replyReceived++;
						}
						
						else if(replyPacket.getPort() == getPort(3, "COMP") || replyPacket.getPort() == getPort(3, "SOEN") || replyPacket.getPort() == getPort(3, "INSE")) {
							instance3 = UDPUtilities.byteArrayToObject(replyPacket.getData());
							System.out.println("Reply received from instance 3");
							replyReceived++;
						}

					}
			    	
			    	return instance1;		    	
			    }

			});
			
			return handler.get(2, TimeUnit.SECONDS);*/
			return UDPUtilities.byteArrayToObject(responses.get(0).getData());
			
		} catch (SocketTimeoutException e) {
			System.out.println("hardware failure");
			return null;
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	private static int getPort(int instance, String departmentName) throws FileNotFoundException, IOException {		
		return Config.getConfig("INSTANCE" + instance + "_" + departmentName + "_PORT");
	}
	
	private static DatagramPacket receiveReply() throws IOException {
		
		byte[] receivedBuffer = new byte[46595];
		DatagramPacket replyPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
		FrontEnd.datagramSocket.receive(replyPacket);		
		return replyPacket;
		
	}
	
}