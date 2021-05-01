package server;

import java.io.*;
import java.net.*;
import java.util.Scanner;


import protocol.*;

public class NodoCentral {

    ServerSocket s;
    int port = 3339;
   
    public NodoCentral() {
    	try {
    		System.out.println("Arrancando el Nodo Central...");
            this.init();
            System.out.println("Abriendo canal de comunicaciones...");
			this.s = new ServerSocket(port);
			while( true ) {
				Socket sServicio = s.accept();
				System.out.println( "Aceptada conexion de " + sServicio.getInetAddress().toString() );
	            ClientHandler clientSock = new ClientHandler(sServicio);
	            new Thread(clientSock).start();
			}
            
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    public static void main(String[] args) {
        new NodoCentral();
    }

    //--------------------------------------------------------------------------

    public void init() {       
        // Codigo de inicializacion ...    	
    }


    
    private static class ClientHandler extends Thread { 
		
	    private final Socket clientSocket; 
	    private ObjectInputStream is;
	    private ObjectOutputStream os;
	    private String PATHFILTERS = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/TrabajoFinalFiltros/src/main/resources/filters.txt";
	    // Constructor 
	    public ClientHandler(Socket socket) 
	    { 
	        this.clientSocket = socket; 
	    } 
	
	    public void run() 
	    { 
	        try {               
	             // start 
	            procesaCliente(clientSocket);
	            
	        }catch (Error e) { 
	            e.printStackTrace(); 
	        } 
	        
	    }

	    //--------------------------------------------------------------------------

	    public void procesaCliente(Socket sServicio) {
	        try {
	        	this.is = new ObjectInputStream(sServicio.getInputStream());
	            this.os = new ObjectOutputStream(sServicio.getOutputStream());

	        	while(true) {
	            
		            
		            PeticionDatos pd = (PeticionDatos)this.is.readObject();
	
	            	if( pd.getSubtipo().compareTo("OP_FILTRO")==0 ) {
	                        this.doFiltering(pd);
	            	}
	        	}

	        } catch (ClassNotFoundException ex) {
	        } catch (IOException ex) {
	        } finally {
	            try {
	                os.close();
	                is.close();
	                sServicio.close();
	            } catch (SocketException ex) {
	            	System.out.println("Broken pipe! Finishing task...");
	            	System.out.println("Ready to use again!");
	            }
	            catch (IOException ex) {
	            	ex.printStackTrace();
	            }
	        }
	    }

	    //--------------------------------------------------------------------------

	    public void doFiltering(PeticionDatos pd) {
	        
	        String filtro = (String) pd.getFiltro();
	        File file = new File(PATHFILTERS);
	    	String last = "";
	    	boolean exists = false;
	    	try {
		    	if (file.exists()) { 
		    		BufferedReader br = new BufferedReader(new FileReader(file)); 
		    		last = br.readLine();
		    		while (last != null) { 
		    			if(last.equals(filtro)) {
		    				exists = true; break;
		    			}
			    		last = br.readLine(); 
		    		}
		    		br.close();
		    		if(exists) {	
		            	System.out.println("El filtro solicitado: " + filtro + " existe!");	            	
		            	RespuestaControl rc = new RespuestaControl("OK");
		            	sleep(25000);
		                this.os.writeObject(rc);  
		                
		                //Aqui deberia quedarse a la escuha del path y solicitar a multiservidores.
		            }else {
		            	System.out.println("El filtro solicitado: " + filtro + " NO existe!");	    
		            	RespuestaControl rc = new RespuestaControl("NOT_OK");
		                this.os.writeObject(rc);  
		            }
	    		} else { 
	    			
		    		System.out.println("No found file"); 
	    		}
	    	} catch (FileNotFoundException e) {
				e.printStackTrace();
	    	} catch (SocketException e) {
	    		e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();		
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
    }
}
