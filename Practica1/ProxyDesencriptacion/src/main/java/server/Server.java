package server;

import java.io.*;
import java.net.*;
import java.util.Scanner;


import protocol.*;

public class Server {

    ServerSocket s;
    int port;
   
    public Server() {
    	try {
    		
    		System.out.println("Inserte puerto de servidor: ");
    		Scanner sc = new Scanner(System.in);
            int port = Integer.parseInt(sc.nextLine());
            sc.close();
    		System.out.println("Arrancando el servidor...");
            this.init();
            System.out.println("Abriendo canal de comunicaciones...");
			this.s = new ServerSocket(port);
			this.port = port;
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
        new Server();
    }

    //--------------------------------------------------------------------------

    public void init() {       
        // Codigo de inicializacion ...    	
    }


    
    private static class ClientHandler extends Thread { 
		
	    private final Socket clientSocket; 
	    private ObjectInputStream is;
	    private ObjectOutputStream os;
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

	            boolean end = false;

	            Peticion p = (Peticion)this.is.readObject();
	            while( !end )
	            {
	            	if(p.getTipo().compareTo("PETICION_DATOS") == 0) {
	                    PeticionDatos pd = (PeticionDatos)p;
	                    if( pd.getSubtipo().compareTo("OP_DESENCRIPTACION")==0 )
	                        this.doDesencriptacion(pd);
	                }
	                p = (Peticion)this.is.readObject();
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

	    public void doDesencriptacion(PeticionDatos pd) {
	        
	        String mensaje = (String) pd.getArgs().get(0);
	        
	        try {
	            if(mensaje != null) {
	            	
	            	System.out.println("DESENCRIPTANDO...");
	            	sleep(25000);	            	
	            	RespuestaControl rc = new RespuestaControl("OK");
	                this.os.writeObject(rc);  
	                System.out.println("DESENCRIPTADO!!");
	            }else {
	            	RespuestaControl rc = new RespuestaControl("NOT_OK");
	                this.os.writeObject(rc);    
	            }
	        } catch (SocketException ex) {
            	
            } catch (Exception ex) {
	        	ex.printStackTrace();
	        }
	        
	    }
    }
}
