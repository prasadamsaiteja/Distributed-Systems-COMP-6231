package generic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UDPUtilities {

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
	
}