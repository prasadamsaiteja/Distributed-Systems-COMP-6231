package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.logging.Logger;

import server.instance3.util.Utils;

public class UDPUtilities {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	//Dont Modify
	public static byte[] objectToByteArray(Object object) {

		try {
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(object);
			return byteOut.toByteArray();			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new byte[1024];
	}

	//Dont Modify
	public static Object byteArrayToObject(byte[] data){
	     
		  try {
			  
			  ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
			  ObjectInputStream in = new ObjectInputStream(byteIn);
			  Object result = (Object) in.readObject();
			  return result;
			  
		  } catch (IOException | ClassNotFoundException e) {
			  e.printStackTrace();
		  }
		     
		  return null;

	 }
	
	/**
	 * Creates & sends the UDP request
	 * 
	 * @param dept
	 * @param info
	 * @param method
	 * @return
	 */
	public static byte[] udpCommunication(String ipAddress,int portNo, Object info, String method, int timeOut) {

		//LOGGER.info("Making UPD Socket Call to " + portNo + " Server for method : " + method);

		// UDP SOCKET CALL AS CLIENT
		HashMap<String, Object> data = new HashMap<>();
		byte[] response = null;
		data.put(method, info);
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			byte[] message = Utils.objectToByteArray(data);
			InetAddress remoteUdpHost = InetAddress.getByName(ipAddress);
			DatagramPacket request = new DatagramPacket(message, message.length, remoteUdpHost, portNo);
			socket.send(request);
			
			if(timeOut == -1)
				return null;
			
			socket.setSoTimeout(timeOut);
			byte[] buffer = new byte[65556];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			socket.receive(reply);
			response = (reply==null)?null:reply.getData();

		} catch (SocketException e) {
			e.printStackTrace();
		} catch(SocketTimeoutException e) {
			e.printStackTrace();
			//LOGGER.info("TIMEOUT - REQUEST TO : "+ipAddress+" @Port : "+portNo);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}

		return response;
	}
	
}