package client;

import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import client.controller.Controller;
import client.controller.data.Cache;
import utils.corbaInterface.IDCRS;
import utils.corbaInterface.IDCRSHelper;

public class Client {	

	public static void main(String[] args) throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		
		Cache cache = Cache.getInstance();
		
		System.out.print("Enter Student/Adivsor Id: ");
		cache.id = new Scanner(System.in).nextLine().toUpperCase();
		
		if(!cache.checkValidId())
			return;
		
		ORB orb = ORB.init(args, null);
	    org.omg.CORBA.Object objRef =   orb.resolve_initial_references("NameService");
	    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	    cache.dcrs = (IDCRS) IDCRSHelper.narrow(ncRef.resolve_str(cache.getDepartment()));
		
		promptUserAction();
		
	}
	
	private static void promptUserAction() {
		
		System.out.println("");
		Cache cache = Cache.getInstance();
		int input;
		
		do {
			
			if(cache.getClientType() == Cache.ClientType.ADVISOR) {
				System.out.println("1. Add Course");
				System.out.println("2. Remove Course");
				System.out.println("3. List Courses Availability");								
				
				System.out.println("\nStudent Actions");
				System.out.println("4. Enroll Course to a student");
				System.out.println("5. Get Class Schedule");
				System.out.println("6. Drop Course");
				System.out.println("7. Swap Course");
			}
			
			else {			
				System.out.println("1. Enroll Course");
				System.out.println("2. Drop Course");
				System.out.println("3. Get Class Schedule");
				System.out.println("4. Swap Course");				
			}
			
			System.out.println("8. Exit");
			System.out.print("\nSelect a action: ");
			
			input = new Scanner(System.in).nextInt();
			performAction(input);
			
			System.out.println("-----------------------------------------------------------------------------------------");
		} while(input != 8);
	}
	
	private static void performAction(int action) {
		
		Cache cache = Cache.getInstance();
		
		switch(action) {
		
			case 1:
				if(cache.getClientType() == Cache.ClientType.ADVISOR) Controller.addCourse();
				else Controller.enrollCourse();
				break;
			
			case 2:	
				if(cache.getClientType() == Cache.ClientType.ADVISOR) Controller.removeCourse();
				else Controller.dropCourse();
				break;
				
			case 3:
				if(cache.getClientType() == Cache.ClientType.ADVISOR) Controller.listCourseAvailable();
				else Controller.getClassSchedule();
				break;
				
			case 4:
				if(cache.getClientType() == Cache.ClientType.ADVISOR) Controller.enrollCourse();
				else Controller.swapCourse();
				break;
				
			case 5:
				if(cache.getClientType() == Cache.ClientType.ADVISOR) Controller.getClassSchedule();
				break;
				
			case 6:
				if(cache.getClientType() == Cache.ClientType.ADVISOR) Controller.dropCourse();
				break;
				
			case 7:
				if(cache.getClientType() == Cache.ClientType.ADVISOR) Controller.swapCourse();
				break;
		}
		
	}

}