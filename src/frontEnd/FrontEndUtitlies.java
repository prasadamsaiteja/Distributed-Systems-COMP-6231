package frontEnd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import utils.Config;
import utils.Constants;
import utils.UDPUtilities;

public class FrontEndUtitlies {
	
	public static Object sendUDPRequest(String message) {
		
		try {
			byte[] buffer = message.getBytes();
			
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(500);
			
			InetAddress sequencerIP = InetAddress.getByName(Config.getStringConfig("SEQUENCER_IP"));
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, sequencerIP, Config.getConfig("SEQUENCER_PORT"));
			socket.send(requestPacket);		
			System.out.println("\nRequest sent to sequencer");
			
		} catch(Exception ex){
			ex.printStackTrace();
		}

		ArrayList<DatagramPacket> responses = receiveReplies();
		if(UDPUtilities.byteArrayToObject(responses.get(0).getData()) instanceof Boolean)
			return getMajorityOfRepliesBoolean(responses, Boolean.class);
		
		if(UDPUtilities.byteArrayToObject(responses.get(0).getData()) instanceof SimpleEntry)
			return getMajorityOfRepliesBoolean(responses, SimpleEntry.class);
		
		if(UDPUtilities.byteArrayToObject(responses.get(0).getData()) instanceof HashMap)
			return getMajorityOfRepliesBoolean(responses, HashMap.class);
		
		return null;
	}
	
	private static <T> Object getMajorityOfRepliesBoolean(ArrayList<DatagramPacket> responses, Class<T> type) {
		
		T instance1, instance2, instance3, instance4;
		T majorityReply = null;
		int majorityReplyCounter = 0;
		
		try {
			
			for (DatagramPacket datagramPacket : responses) {
				
				T currentPacketObject = (T) UDPUtilities.byteArrayToObject(datagramPacket.getData());
				System.out.println(currentPacketObject.toString());
				
				if(majorityReplyCounter == 0) {					
					majorityReply = currentPacketObject;
					majorityReplyCounter++;
				} else if(equals(majorityReply, currentPacketObject)) {
					majorityReplyCounter++;
				} else {
					majorityReplyCounter--;
				}
								
				//TODO ip address check
				if(datagramPacket.getPort() == getPort(1, "COMP") || datagramPacket.getPort() == getPort(1, "SOEN") || datagramPacket.getPort() == getPort(1, "INSE")) {
					instance1 = currentPacketObject;
					System.out.println("Reply received from instance 1");
				}
									
				else if(datagramPacket.getPort() == getPort(2, "COMP") || datagramPacket.getPort() == getPort(2, "SOEN") || datagramPacket.getPort() == getPort(2, "INSE")) {
					instance2 = currentPacketObject;
					System.out.println("Reply received from instance 2");
				}
	
				else if(datagramPacket.getPort() == getPort(3, "COMP") || datagramPacket.getPort() == getPort(3, "SOEN") || datagramPacket.getPort() == getPort(3, "INSE")) {
					instance3 = currentPacketObject;
					System.out.println("Reply received from instance 3");
				} 
				
				else if(datagramPacket.getPort() == getPort(4, "COMP") || datagramPacket.getPort() == getPort(4, "SOEN") || datagramPacket.getPort() == getPort(4, "INSE")) {
					instance4 = currentPacketObject;
					System.out.println("Reply received from instance 4");
				}
			}
			
			if(majorityReplyCounter == responses.size()) {
				System.out.println("Response sent to client");
				return majorityReply;
			}
			
			else{
				//TODO need to find which instance has the software issue
				//TODO background thread to notify replica manager about the software failure
				System.out.println("There is a software issue: " + majorityReplyCounter);
				System.out.println("Response sent to client");
				return majorityReply;
			}
				
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
				
	}

	@SuppressWarnings("unchecked")
	private static <T> boolean equals(T majorityReply, T currentPacketObject) {
		
		if(majorityReply instanceof Boolean)
			return (boolean) majorityReply == (boolean) currentPacketObject;
		
		else if(majorityReply instanceof SimpleEntry) {
			
			boolean temp1 = ((SimpleEntry<Boolean, String>) majorityReply).getKey();
			boolean temp2 = ((SimpleEntry<Boolean, String>) currentPacketObject).getKey();			
			
			return temp1 == temp2;
			
		} else if(majorityReply instanceof HashMap) {
			
			HashMap<String, Integer> temp1 = (HashMap<String, Integer>) majorityReply;
			HashMap<String, Integer> temp2 = (HashMap<String, Integer>) currentPacketObject;
			
			return temp1.equals(temp2);	
			
		}
					
		return false;
	}

	private static ArrayList<DatagramPacket> receiveReplies() {
		
		ArrayList<DatagramPacket> replies = new ArrayList<>();		
		ExecutorService executor = Executors.newSingleThreadExecutor();

		@SuppressWarnings("unchecked")
		Future<ArrayList<DatagramPacket>> handler = executor.submit(new Callable() {
		    @Override
		    public ArrayList<DatagramPacket> call() throws IOException {
		        
		    	int replyReceived = 0;
		    	
		    	while(replyReceived < 2) {						
					DatagramPacket replyPacket = receiveReply();
					replies.add(replyPacket);					
					replyReceived++;
				}
		    	
		    	return replies;
		    }

		});
		
		try {
			//TODO
			//return handler.get(5, TimeUnit.Seconds);
			return handler.get(2, TimeUnit.HOURS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			
			handler.cancel(true);
			System.out.println("Possible Hardware failure detected, notifying replica managers");
			
			IntStream.rangeClosed(1, 3).forEach(i -> {	
				
				new Thread(new Runnable() {
					public void run() {
						try {
							UDPUtilities.udpCommunication(Config.getStringConfig("RM"+i+"_IP"), Config.getConfig("RM"+i+"_PORT"), null, Constants.OP_HARDWARE_CRASH, -1);
						} catch(Exception ignored) {}
					}
				}).start();			
			
			});
			
			// TODO check which replica did not respond
			// TODO background thread to notify replica manager about the hardware failure
			return replies; // Other instances replies
		}
				
	}
	
	private static DatagramPacket receiveReply() throws IOException {
		
		byte[] receivedBuffer = new byte[46595];
		DatagramPacket replyPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
		FrontEnd.datagramSocket.receive(replyPacket);
		return replyPacket;
		
	}
	
	private static int getPort(int instance, String departmentName) throws FileNotFoundException, IOException {
		return Config.getConfig("INSTANCE" + instance + "_" + departmentName + "_PORT");
	}
	
}