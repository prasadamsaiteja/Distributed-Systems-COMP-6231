/*
* COMP6231 - Distributed Systems | Fall2018
* Final Project 
* Professor - Rajagopalan Jayakumar
* Software Failure Tolerant and Highly Available Distributed Course Registration System (DCRS)
*/
package replicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import server.instance1.Instance1Server;
import server.instance2.Instance2Server;
import server.instance3.Instance3Server;
import server.instance3.util.Utils;
import server.instance4.Instance4Server;
import utils.Config;
import utils.Constants;
import utils.UDPUtilities;
import utils.Utility;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target=
 *      "_blank">Profile</a>
 */
public class ReplicaManagerEngine implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	int instanceNo;
	int portNo;
	boolean isAlive;
	Queue<Integer> requestQueue;

	/**
	* 
	*/
	public ReplicaManagerEngine(int instanceNo, int portNo) {
		this.instanceNo = instanceNo;
		this.portNo = portNo;
		this.isAlive = true;
		this.requestQueue = new PriorityQueue<>();
	}

	@Override
	public void run() {

		// run the UDP server
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(portNo);
			byte[] buffer = new byte[1000];// to stored the received data from the client.
			LOGGER.info("Replica Manager for Instance : " + instanceNo + " started at port :" + portNo);

			// non-terminating loop as the server is always in listening mode.
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				// Server waits for the request to come
				socket.receive(request); // request received
				LOGGER.info("Received UDP Socket call from " + request.getPort() + " with address["
						+ request.getAddress() + "]");
				byte[] response = processUDPRequest(request.getData());
				if (response == null)
					continue; // will reply to Front end manually

				DatagramPacket reply = new DatagramPacket(response, response.length, request.getAddress(),
						request.getPort());// reply packet ready
				socket.send(reply); // reply sent
			}
		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			LOGGER.severe("SocketTimeOutException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}

	/**
	 * Handles the UDP request for information
	 * 
	 * @param data
	 * @return
	 */
	private byte[] processUDPRequest(byte[] data) {

		byte[] response = null;
		HashMap<String, Object> request = (HashMap<String, Object>) Utils.byteArrayToObject(data);

		for (String key : request.keySet()) {

			LOGGER.info("Received UDP Socket call for method[" + key + "] with parameters[" + request.get(key) + "]");
			switch (key) {

			case Constants.OP_HARDWARE_CRASH:
				// start the HeartBeat Mechanism
				response = Integer.valueOf(performHeartBeatProtocol()).toString().getBytes();
				break;
			case Constants.OP_ISALIVE:
				if (isAlive)
					response = new String("true").getBytes();
				else
					response = null;
				break;
			case Constants.OP_SOFTWARE_CRASH:
				response = Integer.valueOf(performSoftwareCrashRecovery()).toString().getBytes();
				break;
			default:
				LOGGER.info("NO HANDLER FOR UDP Socket call for method[" + key + "] with parameters[" + request.get(key)
						+ "]");
			}
		}

		return response;
	}

	/**
	 * @return
	 */
	private int performSoftwareCrashRecovery() {

		this.isAlive = false;

		List<Integer> serverInstances = new ArrayList<>();
		IntStream.rangeClosed(1, 4).forEach(i -> {
			if (i != instanceNo) {
				serverInstances.add(i);
			}
		});
		byte[] reply = getDataFromWorkingReplicaManager(serverInstances);
		boolean copy = copyState(reply,instanceNo);
		if (copy == true)
			return instanceNo;
		else
			return -1;
	}

	/**
	 * @return
	 */
	private byte[] getCurrentState(int instance) {
		// TODO change timeout in udp call
		List<byte[]> state = new ArrayList<>();
		state.add(Utility.deepCopyQueue(requestQueue));
		state.add(UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
				Config.getConfig("INSTANCE" + instance + "_COMP_PORT"), null, Constants.OP_GETSTATE, 0));
		state.add(UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
				Config.getConfig("INSTANCE" + instance + "_SOEN_PORT"), null, Constants.OP_GETSTATE, 0));
		state.add(UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
				Config.getConfig("INSTANCE" + instance + "_INSE_PORT"), null, Constants.OP_GETSTATE, 0));
		return UDPUtilities.objectToByteArray(state);
	}

	/**
	 * 
	 */
	private int performHeartBeatProtocol() {
		if (!isAlive()) {
			this.isAlive = false;
			instantiateNewServer();
			List<Integer> serverInstances = new ArrayList<>();
			IntStream.rangeClosed(1, 4).forEach(i -> {
				if (i != instanceNo) {
					serverInstances.add(i);
				}
			});

			byte[] reply = getDataFromWorkingReplicaManager(serverInstances);
			boolean copy = copyState(reply);
			if (copy == true)
				return instanceNo;
			else
				return -1;
		} else {
			return instanceNo;
		}
	}

	/**
	 * @param serverInstances
	 * @return
	 */
	private byte[] getDataFromWorkingReplicaManager(List<Integer> serverInstances) {

		for (Integer i : serverInstances) {

			byte[] reply = null;
			try {
				reply = UDPUtilities.udpCommunication(Config.getStringConfig("RM" + i + "_IP"),
						Config.getConfig("RM" + i + "_PORT"), null, Constants.OP_ISALIVE, 0); // TODO change timeout
			} catch (Exception ignored) {
			}

			if (reply != null && "true".equalsIgnoreCase(new String(reply).trim())) {
				System.out.println("Fetching database from instance " + i);
				return getCurrentState(i);
			}
		}
		return null;
	}

	private boolean isAlive() {
		byte[] reply;

		reply = UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instanceNo + "_IP"),
				Config.getConfig("INSTANCE" + instanceNo + "_COMP_PORT"), null, Constants.OP_ISALIVE, 1000); // TODO
																												// change
																												// timeout

		LOGGER.info("MAKING ISALIVE REQUEST : " + Config.getConfig("INSTANCE" + instanceNo + "_COMP_PORT") + " REPLY -"
				+ reply);

		if (reply == null)
			return false;
		else {
			return Boolean.valueOf(new String(reply).trim());
		}
	}

	private void instantiateNewServer() {
		instantiateNewServer(instanceNo);
	}

	/**
	 * 
	 */
	private void instantiateNewServer(int server) {
		try {
			switch (server) {
			case 1:
				Instance1Server.main(null);
				break;
			case 2:
				Instance2Server.main(null);
				break;
			case 3:
				Instance3Server.main(null);
				break;
			case 4:
				Instance4Server.main(null);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean copyState(byte[] reply){
		return copyState(reply,instanceNo);
	}
	
	/**
	 * @param reply
	 * @return
	 */
	private boolean copyState(byte[] reply,int instance) {
		if (reply == null) {
			System.out.println("RECEIVED NULL COPY STATE");
		} else {
			List<byte[]> state = (List<byte[]>) UDPUtilities.byteArrayToObject(reply);
			this.requestQueue = (Queue<Integer>) UDPUtilities.byteArrayToObject(state.get(0));
			
			System.out.println("Setting state to instance : "+instance);
			
			UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
					Config.getConfig("INSTANCE" + instance + "_COMP_PORT"),
					UDPUtilities.byteArrayToObject(state.get(1)), Constants.OP_SETSTATE, 1000);
			UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
					Config.getConfig("INSTANCE" + instance + "_SOEN_PORT"),
					UDPUtilities.byteArrayToObject(state.get(2)), Constants.OP_SETSTATE, 1000);
			UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
					Config.getConfig("INSTANCE" + instance + "_INSE_PORT"),
					UDPUtilities.byteArrayToObject(state.get(3)), Constants.OP_SETSTATE, 1000);
		}

		System.out.println("State Copied by RM");

		return true;
	}

	private void killServer() {
		try {
			switch (instanceNo) {
			case 1:
				Instance1Server.main(null);
				break;
			case 2:
				// TODO
				break;
			case 3:
				Instance3Server.main(null);
				break;
			case 4:
				// TODO
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
