import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import FilterApp.Filter;
import FilterApp.FilterHelper;
import FilterApp.FilterPOA;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

class FilterImpl extends FilterPOA {
    private ORB orb;
    private int n = 0;
    static String PATH = "/Users/Shared/";
    static String DirPATHResponse = "/Users/Shared/Response/";
    static String DirPATHRequest = "/Users/Shared/Request/request.txt";
    
    private WatchService wService;
    private WatchKey key;
    
    
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
            System.out.println("Read image successfully");
            // -------------------
            
            escriboPathFichero(filter, path);
            
            //ME PONGO A LA ESPERA DE CAMBIO EN FICHERO
            Path directoryPath = FileSystems.getDefault().getPath(DirPATHResponse);
            WatchService wService = FileSystems.getDefault().newWatchService();
			directoryPath.register(wService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
			
            /* Wait until we get some events */
            System.out.println("Waiting for key be signalled with wService.take()");
            String lastline = "";
            key = wService.take();
            if (key.isValid()) {
            	Path pathy = null;
                 for(WatchEvent<?> event: key.pollEvents()) {
                     /* In the case of ENTRY_CREATE and ENTRY_MODIFY events the context is a relative */
                     pathy = (Path)event.context();
                     Kind<?> kindOfEvent = event.kind();
                     System.out.println(String.format("Event '%s' detected in file/directory '%s'", kindOfEvent.name(),pathy));
                 }
                 // Lee el archivo y coge el  path
                 
                 lastline = getFilteredPath(pathy);
                 System.out.println("New PATH: " + lastline);
                 
            }
            /* once an key has been processed,  */
            boolean valid = key.reset();
            System.out.println(String.format("Return value from key.reset() : %s", valid) );
	        
            //--------------------
            /*
            * 	LLAMADA AL FILTRO CORRESPONDIENTE
            */

            //creo path para la imagen filtrada
//            dstPath = PATH + "filtered" + n + ".jpg";
//            File dstFile = new File(dstPath);
//            ImageIO.write(bf, "jpg", dstFile);
//            System.out.println("Filtro " + filter + " aplicado. Nueva imagen guardada en: " + dstPath);
//              
//            // Eliminar imagen anterior
//            File oldPath = new File(PATH + "filtered" + (n - 1) + ".jpg");
//            oldPath.delete();
//              
//            n++;
            return lastline;
            
        } catch (IIOException e) {
            System.out.println("File not found.");
            return "";
        } catch (IOException e) {
            System.out.println("File not found.");
            e.printStackTrace();
            return "";
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 return "";
		}
  
    }
    
    public String getFilteredPath(Path pathy){
    	String path = DirPATHResponse + pathy.toString();
    	System.out.println("Lets read path" + path);
    	File file = new File(path);
    	String last = "";
    	String lastLine = "";
    	try {
	    	if (file.exists()) { 
	    		BufferedReader br = new BufferedReader(new FileReader(file)); 
	    		last = br.readLine(); 
	    		while (last != null) { 
		    		lastLine = last; 
		    		last = br.readLine(); 
	    		} 
	    		br.close();
    		} else { 
	    		System.out.println("No found file"); 
    		}
	    	
	    	return lastLine;
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
    }
    
    public void escriboPathFichero(String filter, String path){
    	File file = new File(DirPATHRequest);
    	try {
	    	if (file.exists()) { 
	    		BufferedWriter br = new BufferedWriter(new FileWriter(file)); 
	    		br.write(filter + ";" + path);
	    		br.close();
    		} else { 
	    		System.out.println("No found file"); 
    		}    	
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
            while(true) {
              orb.run();
            }
        }catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
          
        System.out.println("FilterServer Exiting ...");
        
    }
}