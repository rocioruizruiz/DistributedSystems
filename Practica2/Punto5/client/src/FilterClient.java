// Copyright and License 
 
import FilterApp.*;
import protocol.PeticionControl;
import protocol.RespuestaControl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.omg.CosNaming.*;
import org.omg.CORBA.*;

public class FilterClient
{
	static Filter filterImpl;
  
	static ServerSocket proxy;
	private static int proxyPort = 3338;
  
	public FilterClient(String[] args) {
		try{
			proxy = new ServerSocket(proxyPort);
			System.out.println("Arrancando el servidor proxy...");
			System.out.println("Abriendo canal de comunicaciones Proxy...");
      
			// Proxy Server always running
			while( true ) {
				Socket sServicio = proxy.accept();
				System.out.println( "Aceptada conexion de " + sServicio.getInetAddress().toString() );
				ClientHandler clientSock = new ClientHandler(sServicio, args);
				new Thread(clientSock).start();
			}
      
		} catch(SocketException ex) {
			System.out.println("BAD CONECTION");
  	
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}
	
  	public static void main(String args[]){
  		new FilterClient(args);
  	}



	private static class ClientHandler extends Thread { 
		
	    private final Socket clientSocket; 
	    private String[] args;
	    private ObjectInputStream client_is, proxy_is;
	    private ObjectOutputStream client_os, proxy_os;
	    private PeticionControl peticionCliente;
	    
	    // Constructor 
	    public ClientHandler(Socket socket, String[] args) 
	    { 
	        this.clientSocket = socket; 
	        this.args = args;
	    } 
	
	    public void run() 
	    { 
	        //this.init();
	        try {               
	             // start 
	            procesaCliente(clientSocket, args);
	            
	        }catch (ClassNotFoundException e) { 
	            e.printStackTrace(); 
	        }catch (IOException e){
	        	e.printStackTrace(); 
	        }
	        
	    }
	
	    public void procesaCliente(Socket sServicio, String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
	        try {
	        	// proxy actua como cliente** ante los servidores
	        	
	        	this.client_is = new ObjectInputStream( clientSocket.getInputStream() );
	        	this.client_os = new ObjectOutputStream( clientSocket.getOutputStream() );
	        	String destPath = "";
	        	
	        	peticionCliente = (PeticionControl)this.client_is.readObject();
	        	if(peticionCliente.getSubtipo().equals("OP_FILTER") && !peticionCliente.getPath().isEmpty()) {
	        		System.out.println("Operacion recibida!!");
	        		ArrayList<String> filters = peticionCliente.getArgs();
	        		System.out.println(filters.toString());
	        		String path = peticionCliente.getPath();
	        		//AQUI IRIA EL FOR DE FILTERS PARA EL CASO 1 QUE DIJO FRAN. SE HACE UNA PETICION CORBA POR FILTRO CADA FILTRO.
	        		for(int i=0; i < filters.size(); i++) {
	        		// TODO LO DE CORBA -----------------------
			            try {
			            	// create and initialize the ORB
			            	ORB orb = ORB.init(args, null);
			      
			            	// get the root naming context
			            	org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			            	// Use NamingContextExt instead of NamingContext. This is 
			            	// part of the Interoperable naming Service.  
			            	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			       
			            	// resolve the Object Reference in Naming
			            	String name = "Filter";
			            	filterImpl = FilterHelper.narrow(ncRef.resolve_str(name));
			      
			            	System.out.println("Obtained a handle on server object: " + filterImpl);
			            	String filter = filters.get(i);
			            	
			            	
			            	System.out.println(filter +  " "+ path);
			            	
			            	destPath = filterImpl.applyFilter(filter, path);
			            	// filterImpl.shutdown();
			            
			            	path = destPath;
			            } catch (Exception e) {
			                System.out.println("ERROR : " + e) ;
			                e.printStackTrace(System.out);
		                }
		        		// ----------------------------------------
			            System.out.println("Aplicado el filtro: " + filters.get(i) + " y guardado en el path: " + destPath);
	        		}
	            	RespuestaControl rc = new RespuestaControl("OK", destPath); // Aqui iria el path final que nos devuelve corba
	                this.client_os.writeObject(rc);              	
	    		}else {	        		
	        		System.out.println("Operación no reconocida");
	        		RespuestaControl rc = new RespuestaControl("NOT_OK"); // Aqui iria el path final que nos devuelve corba
	                this.client_os.writeObject(rc);       	
	    		}
	        	
	        	//Asignar servidor
	            
	        } catch(IOException e){
	        	e.printStackTrace();
	        } finally {
	            try {
	                if(proxy_os != null) proxy_os.close();
	                if(proxy_is != null)proxy_is.close();
	                if(sServicio != null)sServicio.close();
	            } catch (IOException ex) {
	            	ex.printStackTrace();
	            }
	        }
	    }
	}
}
	
