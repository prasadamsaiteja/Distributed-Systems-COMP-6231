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

import server.instance1.Instance1Server;
import server.instance3.Instance3Server;
import server.instance3.util.Utils;
import utils.Config;
import utils.Constants;
import utils.UDPUtilities;
import utils.Utility;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target=
 *      "_blank">Profile</a>
 */
public class ReplicaManager implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	int instanceNo;
	int portNo;
	boolean isAlive;
	Queue<Integer> requestQueue;

	/**
	* 
	*/
	public ReplicaManager(int instanceNo, int portNo) {
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
				LOGGER.info("Received UDP Socket call from "+request.getPort()+" with address[" + request.getAddress() + "]");
				byte[] response = processUDPRequest(request.getData());
				if (response == null)
					continue; // will reply to Front end manually

				DatagramPacket reply = new DatagramPacket(response, response.length, request.getAddress(),
						request.getPort());// reply packet ready
				socket.send(reply);	// reply sent
			}
		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch(SocketTimeoutException e){
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
				LOGGER.info("NO HANDLER FOR UDP Socket call for method[" + key + "] with parameters[" + request.get(key) + "]");
			}
		}

		return response;
	}

	/**
	 * @return
	 */
	private int performSoftwareCrashRecovery() {
		
		this.isAlive = false;
		
		instantiateNewServer();
		byte[] reply = getDataFromWorkingReplicaManager();
		boolean copy = copyState(reply);
		if(copy==true)
			return instanceNo;
		else
			return -1;
		
	}

	/**
	 * @return
	 */
	private byte[] getCurrentState(int instance) {
		//TODO change timeout in udp call
		List<byte[]> state = new ArrayList<>();
		state.add(Utility.deepCopyQueue(requestQueue));
		state.add(UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
				Config.getConfig("INSTANCE" + instance + "_COMP_PORT"), null, Constants.OP_GETSTATE,0));
		state.add(UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
				Config.getConfig("INSTANCE" + instance + "_SOEN_PORT"), null, Constants.OP_GETSTATE,0));
		state.add(UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instance + "_IP"),
				Config.getConfig("INSTANCE" + instance + "_INSE_PORT"), null, Constants.OP_GETSTATE,0));
		return UDPUtilities.objectToByteArray(state);
	}

	/**
	 * 
	 */
	private int performHeartBeatProtocol() {
		if (!isAlive()) {
			this.isAlive = false;
			instantiateNewServer();
			byte[] reply = getDataFromWorkingReplicaManager();
			boolean copy = copyState(reply);
			if(copy==true)
				return instanceNo;
			else
				return -1;
		}else {
			return instanceNo;
		}
		
	}

	/**
	 * @return
	 */
	private byte[] getDataFromWorkingReplicaManager() {

		for (int i = 1; i <= 4; i++) {
			if (i == instanceNo || i==2||i==4)
				continue;

			byte[] reply = UDPUtilities.udpCommunication(Config.getStringConfig("RM" + i + "_IP"),
					Config.getConfig("RM" + i + "_PORT"), null, Constants.OP_ISALIVE,0);	//TODO change timeout
			if(reply!=null &&  "true".equalsIgnoreCase(new String(reply).trim())) {
				return getCurrentState(i);
			}
		}
		return null;
	}
	
	private boolean isAlive() {
		byte[] reply;
		
		reply = UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instanceNo + "_IP"),
				Config.getConfig("INSTANCE" + instanceNo + "_COMP_PORT"), null, Constants.OP_ISALIVE,500);
		LOGGER.info("MAKING ISALIVE REQUEST : "+Config.getConfig("INSTANCE" + instanceNo + "_COMP_PORT")+" REPLY -"+reply);
		
		if (reply == null)
			return false;
		else {
			LOGGER.info("---> "+new String(reply));
			return Boolean.valueOf(new String(reply));
		}
	}

	/**
	 * 
	 */
	private void instantiateNewServer() {
		try {
			switch(instanceNo) {
				case 1:
					Instance1Server.main(null);
					break;
				case 2:
					//TODO
					break;
				case 3:
					Instance3Server.main(null);
					break;
				case 4:
					//TODO
					break;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param reply
	 * @return 
	 */
	private boolean copyState(byte[] reply) {
		if (reply == null) {
			System.out.println("RECEIVED NULL COPY STATE");
		} else {
			List<byte[]> state = (List<byte[]>) UDPUtilities.byteArrayToObject(reply);
			this.requestQueue = (Queue<Integer>) UDPUtilities.byteArrayToObject(state.get(0));

			UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instanceNo + "_IP"),
					Config.getConfig("INSTANCE" + instanceNo + "_COMP_PORT"), UDPUtilities.objectToByteArray(UDPUtilities.byteArrayToObject(state.get(1))), Constants.OP_SETSTATE,
					1000);
			UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instanceNo + "_IP"),
					Config.getConfig("INSTANCE" + instanceNo + "_SOEN_PORT"), UDPUtilities.objectToByteArray(UDPUtilities.byteArrayToObject(state.get(2))), Constants.OP_SETSTATE,
					1000);
			UDPUtilities.udpCommunication(Config.getStringConfig("INSTANCE" + instanceNo + "_IP"),
					Config.getConfig("INSTANCE" + instanceNo + "_INSE_PORT"), UDPUtilities.objectToByteArray(UDPUtilities.byteArrayToObject(state.get(3))), Constants.OP_SETSTATE,
					1000);
		}
		
		System.out.println("State Copied by RM");
		
		return true;
	}
}
