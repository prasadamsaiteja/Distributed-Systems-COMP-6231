package frontEnd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
			socket.setSoTimeout(2000);

			InetAddress sequencerIP = InetAddress.getByName(Config.getStringConfig("SEQUENCER_IP"));
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, sequencerIP,
					Config.getConfig("SEQUENCER_PORT"));
			socket.send(requestPacket);
			System.out.println("\nRequest sent to sequencer");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		List<DatagramPacket> responses = receiveReplies();
		
		if (UDPUtilities.byteArrayToObject(responses.get(0).getData()) instanceof Boolean)
			return getMajorityReply(responses, Boolean.class);

		if (UDPUtilities.byteArrayToObject(responses.get(0).getData()) instanceof SimpleEntry)
			return getMajorityReply(responses, SimpleEntry.class);

		if (UDPUtilities.byteArrayToObject(responses.get(0).getData()) instanceof HashMap)
			return getMajorityReply(responses, HashMap.class);

		return null;
	}

	private static <T> Object getMajorityReply(List<DatagramPacket> responses, Class<T> type) {
return null;
		

	}

	@SuppressWarnings("unchecked")
	private static <T> boolean equals(T majorityReply, T currentPacketObject) {

		if (majorityReply instanceof Boolean)
			return (boolean) majorityReply == (boolean) currentPacketObject;

		else if (majorityReply instanceof SimpleEntry) {

			boolean temp1 = ((SimpleEntry<Boolean, String>) majorityReply).getKey();
			boolean temp2 = ((SimpleEntry<Boolean, String>) currentPacketObject).getKey();

			return temp1 == temp2;

		} else if (majorityReply instanceof HashMap) {

			HashMap<String, Integer> temp1 = (HashMap<String, Integer>) majorityReply;
			HashMap<String, Integer> temp2 = (HashMap<String, Integer>) currentPacketObject;

			return temp1.equals(temp2);

		}

		return false;
	}

	private static List<DatagramPacket> receiveReplies() {

		List<DatagramPacket> replies = new ArrayList<>();
		ExecutorService executor = Executors.newSingleThreadExecutor();

		Future<List<DatagramPacket>> handler = executor.submit(new Callable<List<DatagramPacket>>() {
			@Override
			public List<DatagramPacket> call() throws IOException {

				int replyReceived = 0;

				while (replyReceived < 3) {
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
			System.out.println("hardware failure");
			IntStream.rangeClosed(1, 4).forEach(i -> {
				UDPUtilities.udpCommunication(Config.getStringConfig("RM"+i+"_IP"),Config.getConfig("RM"+i+"_PORT"),null,Constants.OP_HARDWARE_CRASH,5000);
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