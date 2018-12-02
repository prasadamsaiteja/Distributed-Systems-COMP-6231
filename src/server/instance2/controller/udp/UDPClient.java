package server.instance2.controller.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

import utils.Config;

public class UDPClient {

	private Integer SOENPort = Config.getConfig("INSTANCE2_SOEN_PORT");
	private Integer INSEPort = Config.getConfig("INSTANCE2_INSE_PORT");
	private Integer COMPPort = Config.getConfig("INSTANCE2_COMP_PORT");
	
	private static HashMap<String, Integer> serverRepo;

	public String udpRequest(String dept, String dummy) {
		serverRepo = new HashMap<>();
		serverRepo.put("COMP", COMPPort);
		serverRepo.put("INSE", INSEPort);
		serverRepo.put("SOEN", SOENPort);

		DatagramSocket ds = null;
		DatagramPacket request1 = null;
		DatagramPacket reply1 = null;
		String response1 = new String();
		try {
			byte[] message = dummy.getBytes();
			byte[] buffer2 = new byte[1000];
			InetAddress aHost = InetAddress.getByName("localhost");

			ds = new DatagramSocket();
			if (dept.contains("COMP"))
				request1 = new DatagramPacket(message, message.length, aHost, (COMPPort));
			
			else if (dept.contains("SOEN"))
				request1 = new DatagramPacket(message, message.length, aHost, SOENPort);
			
			else if (dept.contains("INSE"))
				request1 = new DatagramPacket(message, message.length, aHost, INSEPort);

			ds.send(request1);

			reply1 = new DatagramPacket(buffer2, buffer2.length);
			ds.receive(reply1);
			response1 = new String(reply1.getData());
			response1 = response1.trim();

			ds.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ds != null)
				ds.close();
		}

		return response1;
	}

}
