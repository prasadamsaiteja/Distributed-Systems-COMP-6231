package frontEnd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import utils.Utility;

public class FrontEndUtitlies {

	static Map<Integer, Integer> softwareFailureCount;

	static {
		softwareFailureCount = new HashMap<>();
		IntStream.rangeClosed(1, 4).forEach(i -> {
			softwareFailureCount.put(i, 0);
		});
	}

	public static Object sendUDPRequest(String message) {

		try {
			byte[] buffer = message.getBytes();

			DatagramSocket socket = new DatagramSocket();
			//socket.setSoTimeout(500);

			InetAddress sequencerIP = InetAddress.getByName(Config.getStringConfig("SEQUENCER_IP"));
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, sequencerIP,
					Config.getConfig("SEQUENCER_PORT"));
			socket.send(requestPacket);
			System.out.println("\nRequest sent to sequencer");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		List<DatagramPacket> responses = receiveReplies();
		return replyMajorityResponse(responses);
	}

	/**
	 * @return
	 */
	private static Object replyMajorityResponse(List<DatagramPacket> responses) {

		Map<Integer, Object> instanceResponse = new HashMap<>();
		int instanceNo;
		Object reply;
		for (DatagramPacket dp : responses) {

			instanceNo = Utility.getInstanceNumber(Config.getReverseMaping(dp.getPort()));
			System.out.println("Reply received from instance " + instanceNo);
			reply = UDPUtilities.byteArrayToObject(dp.getData());
			instanceResponse.put(instanceNo, reply);

		}

		return findMajority(instanceResponse);
	}

	/**
	 * @param instanceResponse
	 * @return
	 */
	private static Object findMajority(Map<Integer, Object> instanceResponse) {
		Object reply;
		if (instanceResponse.get(1) instanceof Boolean) {
			reply = getBooleanMajority(instanceResponse);
		} else if (instanceResponse.get(1) instanceof SimpleEntry) {
			reply = getSimpleEntryMajority(instanceResponse);
		} else if (instanceResponse.get(1) instanceof HashMap) {
			reply = getHashMapMajority(instanceResponse);
		} else {
			System.out.println(" FRONTENDUTILITITES : RESPONSE NOT RECOGNIZED ");
			reply = null;
		}

		
		
		// check for software failure
		for (Integer i : softwareFailureCount.keySet()) {
			
			if(softwareFailureCount.get(i) > 0)
				System.out.println("Software Failure occured instance " + i + " (" + softwareFailureCount.get(i) + " times)");
			
			if (softwareFailureCount.get(i) >= 3) {
				softwareFailureCount.put(i, 0);
				
				System.out.println("Software Failure occured instance " + i + ", reported to replica manager");
				UDPUtilities.udpCommunication(Config.getStringConfig("RM" + i + "_IP"),
						Config.getConfig("RM" + i + "_PORT"), null, Constants.OP_SOFTWARE_CRASH, 5000);
				break;
			}
		}

		return reply;
	}

	/**
	 * @param instanceResponse
	 * @return
	 */
	private static <T> Object getHashMapMajority(Map<Integer, Object> instanceResponse) {

		Map<String, List<Integer>> majority = new HashMap<>();
		Map<String, HashMap<String, T>> reply = new HashMap<>();
		for (Integer i : instanceResponse.keySet()) {

			HashMap<String, T> s = (HashMap<String, T>) instanceResponse.get(i);
			List<String> keys = new ArrayList<>();
			for (String k : s.keySet()) {
				keys.add(k);
			}
			Collections.sort(keys);
			StringBuilder concatString = new StringBuilder();
			;
			keys.stream().forEach(xs -> {
				concatString.append(xs);
			});
			;

			reply.put(concatString.toString(), s);

			if (majority.containsKey(concatString)) {
				List<Integer> list = majority.get(i);
				list.add(i);
				majority.put(concatString.toString(), list);
			} else {
				List<Integer> instances = new ArrayList<>();
				instances.add(i);
				majority.put(concatString.toString(), instances);
			}

		}

		int max = -1;
		Map<String, T> maxReply = null;
		String faultReply = null;

		for (String key : majority.keySet()) {

			if (max < majority.get(key).get(0)) {
				max = majority.get(key).get(0);
				maxReply = reply.get(key);
			} else {
				faultReply = key;
			}
		}

		if (faultReply != null) {
			int faultInstance = majority.get(faultReply).get(0);

			for (Integer i : softwareFailureCount.keySet()) {
				if (i == faultInstance) {
					softwareFailureCount.put(faultInstance, (softwareFailureCount.get(faultInstance) + 1));
				} else {
					softwareFailureCount.put(i, 0);
				}
			}

		}

		return maxReply;

	}

	/**
	 * @param instanceResponse
	 * @return
	 */
	private static Object getSimpleEntryMajority(Map<Integer, Object> instanceResponse) {

		Map<Boolean, List<Integer>> majority = new HashMap<>();
		for (Integer i : instanceResponse.keySet()) {

			SimpleEntry<Boolean, String> s = (SimpleEntry<Boolean, String>) instanceResponse.get(i);
			Boolean b = s.getKey();
			if (majority.containsKey(b)) {
				List<Integer> list = majority.get(b);
				list.add(i);
				majority.put(b, list);
			} else {
				List<Integer> instances = new ArrayList<>();
				instances.add(i);
				majority.put(b, instances);
			}

		}

		int max = -1;
		Boolean maxReply = null;
		Boolean faultReply = null;

		for (Boolean key : majority.keySet()) {

			if (max < majority.get(key).get(0)) {
				max = majority.get(key).get(0);
				maxReply = key;
			} else {
				faultReply = key;
			}
		}
		SimpleEntry<Boolean, String> reply=null;
		for (Integer i : instanceResponse.keySet()) {
			SimpleEntry<Boolean, String> s = (SimpleEntry<Boolean, String>) instanceResponse.get(i);
			if(s.getKey()==maxReply) {
				reply = s;
				break;
			}
		}
		

		if (faultReply != null) {
			int faultInstance = majority.get(faultReply).get(0);

			for (Integer i : softwareFailureCount.keySet()) {
				if (i == faultInstance) {
					softwareFailureCount.put(faultInstance, (softwareFailureCount.get(faultInstance) + 1));
				} else {
					softwareFailureCount.put(i, 0);
				}
			}

		}

		return reply;
	}

	/**
	 * @param instanceResponse
	 * @return
	 */
	private static Object getBooleanMajority(Map<Integer, Object> instanceResponse) {

		Map<Boolean, List<Integer>> majority = new HashMap<>();

		for (Integer i : instanceResponse.keySet()) {

			Boolean b = (Boolean) instanceResponse.get(i);
			if (majority.containsKey(b)) {
				List<Integer> list = majority.get(b);
				list.add(i);
				majority.put(b, list);
			} else {
				List<Integer> instances = new ArrayList<>();
				instances.add(i);
				majority.put(b, instances);
			}

		}

		int max = -1;
		Boolean maxReply = null;
		Boolean faultReply = null;

		for (Boolean key : majority.keySet()) {
			if (max < majority.get(key).get(0)) {
				max = majority.get(key).get(0);
				maxReply = key;
			} else {
				faultReply = key;
			}
		}

		if (faultReply != null) {
			int faultInstance = majority.get(faultReply).get(0);

			for (Integer i : softwareFailureCount.keySet()) {
				if (i == faultInstance) {
					softwareFailureCount.put(faultInstance, (softwareFailureCount.get(faultInstance) + 1));
				} else {
					softwareFailureCount.put(i, 0);
				}
			}

		}

		return maxReply;
	}

	private static List<DatagramPacket> receiveReplies() {

		List<DatagramPacket> replies = new ArrayList<>();
		ExecutorService executor = Executors.newSingleThreadExecutor();

		@SuppressWarnings("unchecked")
		Future<List<DatagramPacket>> handler = executor.submit(new Callable<List<DatagramPacket>>() {
			@Override
			public List<DatagramPacket> call() throws IOException {

				int replyReceived = 0;

				while (replyReceived < 4) {
					DatagramPacket replyPacket = receiveReply();
					replies.add(replyPacket);
					replyReceived++;
				}
				return replies;
			}
		});

		try {
			return handler.get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {

			handler.cancel(true);
			System.out.println("Possible Hardware failure detected, notifying replica managers");

			IntStream.rangeClosed(1, 4).forEach(i -> {
				
				new Thread(new Runnable() {
					public void run() {
						try {
							
							UDPUtilities.udpCommunication(Config.getStringConfig("RM" + i + "_IP"),
									Config.getConfig("RM" + i + "_PORT"), null, Constants.OP_HARDWARE_CRASH, 0);
							
						} catch (Exception ignored) {
						}
					}
				}).start();
				
			});

			return replies;
		}

	}

	private static DatagramPacket receiveReply() throws IOException {

		byte[] receivedBuffer = new byte[46595];
		DatagramPacket replyPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
		FrontEnd.datagramSocket.receive(replyPacket);
		return replyPacket;

	}

}