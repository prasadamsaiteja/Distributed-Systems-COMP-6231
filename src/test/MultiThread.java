package test;



import java.util.AbstractMap.SimpleEntry;


import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import client.controller.data.Cache;
import utils.corbaInterface.IDCRS;
import utils.corbaInterface.IDCRSHelper;


public class MultiThread {

	public static void main(String[] args) throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

		Cache cache = Cache.getInstance();
		cache.id = "COMPA1111";
		if(!cache.checkValidId())
			return;
	
		ORB orb = ORB.init(args, null);
	    org.omg.CORBA.Object objRef =   orb.resolve_initial_references("NameService");
	    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	    cache.dcrs = (IDCRS) IDCRSHelper.narrow(ncRef.resolve_str(cache.getDepartment()));
	    cache.dcrs.addCourse(cache.id, "COMP6164", "FALL", 2);
	    cache.dcrs.addCourse(cache.id, "COMP6163", "FALL", 2);
	    cache.dcrs.addCourse(cache.id, "COMP6164", "WINTER", 2);
	    cache.dcrs.addCourse(cache.id, "COMP6164", "SUMMER", 2);
		Runnable task1 = () -> {
			Any any = cache.dcrs.enrolCourse("COMPS1111", "COMP6164", "FALL");
			SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) any.extract_Value();
			System.out.println("Enrolment result: " + result);
		};
		Runnable task2 = () -> {
			Any any = cache.dcrs.enrolCourse("COMPS2222", "COMP6164", "FALL");
			SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) any.extract_Value();
			System.out.println("Enrolment result: " + result);
		};

		Runnable task3 = () -> {
			Any any = cache.dcrs.enrolCourse("COMPS3333", "COMP6164", "FALL");
			SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) any.extract_Value();
			System.out.println("Enrolment result: " + result);
		};

		Runnable task4 = () -> {
			Any any = cache.dcrs.swapCourse("COMPS3333", "COMP6164", "COMP6163");
			SimpleEntry<Boolean, String> result = (SimpleEntry<Boolean, String>) any.extract_Value();
			System.out.println("Swapcourse result: " + result);
		};
			
		task1.run();
		task2.run();
		task3.run();
		task4.run();
			
	  
	    
	}

}
