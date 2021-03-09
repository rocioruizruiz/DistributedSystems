package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;


public class Proxy {
	
	static ArrayList<Pair<Integer, ArrayList<Socket>>> portSockets = new ArrayList<Pair<Integer, ArrayList<Socket>>>();
	ServerSocket proxy;
	private static int proxyPort = 3338;
	
	public Proxy(){				
        try {
        	getServers(new File("/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/ComunicacionProcesos/src/server/servers.txt"));
        	this.proxy = new ServerSocket(proxyPort);
    		System.out.println("Arrancando el servidor proxy...");
            //this.init();
            System.out.println("Abriendo canal de comunicaciones Proxy...");
            
            // Proxy Server always running
            while( true ) {
                Socket sServicio = proxy.accept();
                System.out.println( "Aceptada conexion de " + sServicio.getInetAddress().toString() );
                ClientHandler clientSock = new ClientHandler(sServicio);
                new Thread(clientSock).start();
            }
        } catch (IOException ex) {
        	ex.printStackTrace();
		} 
	}
	//---------------------------------------//
	
	public static void main(String[] args) {
		new Proxy();
	}
	
	//---------------------------------------//
	
	public void getServers(File archivo) throws NumberFormatException, IOException {
		String cadena;
        FileReader f = new FileReader(archivo);
        BufferedReader b = new BufferedReader(f);
        while((cadena = b.readLine())!=null) {
            Proxy.portSockets.add(new Pair(Integer.parseInt(cadena), null));
        }
        System.out.println(Proxy.portSockets.toString());
        b.close();
    }
	
	//---------------------------------------//
	
	private static class ClientHandler extends Thread { 
			
	    private final Socket clientSocket; 
	    private Socket proxySocket;
	    private ObjectInputStream client_is, proxy_is;
	    private ObjectOutputStream client_os, proxy_os;
	    private Usuario usuario;
		private FileUsers fileUsers;
	    
	    // Constructor 
	    public ClientHandler(Socket socket) 
	    { 
	        this.clientSocket = socket; 
	    } 
	
	    public void run() 
	    { 
	        //this.init();
	        try {               
	             // start 
	            procesaCliente(clientSocket);
	            
	        }catch (Error e) { 
	            e.printStackTrace(); 
	        } 
	        
	    }
	    public void procesaCliente(Socket sServicio) {
	        try {           
	        	// proxy actua como cliente** ante los servidores
	        	int serv = new Random().nextInt(4);
	        	
	        	System.out.println("HEY - BEFORE CLIENT TUBE PROXY");
	        	
	        	this.client_is = new ObjectInputStream( clientSocket.getInputStream() );
	        	this.client_os = new ObjectOutputStream( clientSocket.getOutputStream() );
	        	
	        	System.out.println("HEY - after CLIENT TUBE PROXY");
	        	
	        	this.proxySocket = new Socket("localhost", (Integer)Proxy.portSockets.get(serv).getL()); //creamos comunciacion con servidor aleatoria --cambiar
                //Asociamos los objetos al socket
	        	
	        	System.out.println("HEY - BEFORE PROXY TUBE PROXY");
	        	
	        	boolean end =  false;
	        	
	        		this.proxy_os = new ObjectOutputStream( proxySocket.getOutputStream() );
		        	System.out.println("HEY - AFTER OBJECT output PROXY-SERVER");
		        	this.proxy_is = new ObjectInputStream( proxySocket.getInputStream() );
		        	System.out.println("HEY - AFTER OBJECT INPUT PROXY-SERVER");
		        	
		        while(!end) {
		        	System.out.println("HEY - after PROXY TUBE PROXY");
		        	
	        		this.proxy_os.writeObject(this.client_is.readObject());
		        	//this.proxy_is.readObject();
		        	System.out.println("HEY - AFTER proxy WRITE OBJECT");
		        	this.client_os.writeObject(this.proxy_is.readObject());
		        	System.out.println("HEY - AFTER CLIENT WRITE OBJECT");
	        	}
	        	
	        	
	        	
	        	
	        	
	        	
	        }catch(IOException | ClassNotFoundException e){
	        	
	        }finally {
	            try {
	                proxy_os.close();
	                proxy_is.close();
	                sServicio.close();
	            } catch (IOException ex) {
	            }
	        }
	    }
	}
}
