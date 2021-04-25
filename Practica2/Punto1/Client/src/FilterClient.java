// Copyright and License 
 
import FilterApp.*;
import org.omg.CosNaming.*;

import java.util.Scanner;

import org.omg.CORBA.*;

public class FilterClient
{
  static Filter filterImpl;
  static String PATH = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/CORBA_SERVER-P2/src/";
  static String IMAGENAME = "tierra.jpg";
  public static void main(String args[])
    {
      try{
        // create and initialize the ORB
        ORB orb = ORB.init(args, null);

        // get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        // Use NamingContextExt instead of NamingContext. This is 
        // part of the Interoperable naming Service.  
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
 
        // resolve the Object Reference in Naming
        String name = "Filter";
        filterImpl = FilterHelper.narrow(ncRef.resolve_str(name));

        System.out.println("Obtained a handle on server object: " + filterImpl);
        String filter  = "EscalaDeGrises";
        String path = PATH + IMAGENAME;

        
        System.out.println(filterImpl.applyFilter(filter, path));
        filterImpl.shutdown();

        } catch (Exception e) {
          System.out.println("ERROR : " + e) ;
          e.printStackTrace(System.out);
          }
    }

}

