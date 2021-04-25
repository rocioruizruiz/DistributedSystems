import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import FilterApp.Filter;
import FilterApp.FilterHelper;
import FilterApp.FilterPOA;

class FilterImpl extends FilterPOA {
  private ORB orb;
  private int n = 0;
  static String PATH = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/CORBA_SERVER-P2/src/";

  public void setORB(ORB orb_val) {
    orb = orb_val; 
  }
    

  public String applyFilter(String filter, String path) {
	  //abro archivo
 
	  BufferedImage bf = null;
	  File file = null; 
	  String dstPath = "";
	  try {
		  
		  file = new File(path);
		  bf = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		  bf = ImageIO.read(file);
		  System.out.println("read file successfully");
	  } catch (Exception e) {
	      System.out.println("Esta mal el path o q");
	  }

	  //creo path para la imagen filtrada
	  try {
		  dstPath = PATH + "filtered" + n + ".jpg";
		  File dstFile = new File(dstPath);
	      ImageIO.write(bf, "png", dstFile);
	    }
	    catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
	  return "DONE: PATH/" + "filtered" + n + ".jpg";
  }
  


// implement shutdown() method
  public void shutdown() {
	  orb.shutdown(false);
  }
  
  
}


public class FilterServer {

  public static void main(String args[]) {
    try{
      // create and initialize the ORB
      ORB orb = ORB.init(args, null);

      // get reference to rootpoa & activate the POAManager
      POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      rootpoa.the_POAManager().activate();

      // create servant and register it with the ORB
      FilterImpl filterImpl = new FilterImpl();
      filterImpl.setORB(orb); 

      // get object reference from the servant
      org.omg.CORBA.Object ref = rootpoa.servant_to_reference(filterImpl);
      Filter href = FilterHelper.narrow(ref);
          
      // get the root naming context
      // NameService invokes the name service
      org.omg.CORBA.Object objRef =
          orb.resolve_initial_references("NameService");
      // Use NamingContextExt which is part of the Interoperable
      // Naming Service (INS) specification.
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

      // bind the Object Reference in Naming
      String name = "Filter";
      NameComponent path[] = ncRef.to_name( name );
      ncRef.rebind(path, href);

      System.out.println("FilterServer ready and waiting ...");

      // wait for invocations from clients
      orb.run();
    } 
        
      catch (Exception e) {
        System.err.println("ERROR: " + e);
        e.printStackTrace(System.out);
      }
          
      System.out.println("FilterServer Exiting ...");
        
  }
}