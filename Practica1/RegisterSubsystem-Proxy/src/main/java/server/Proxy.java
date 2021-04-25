package server;


import static com.mongodb.client.model.Filters.eq;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;


import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.conection.ProxyConnection;

import protocol.PeticionControl;
import protocol.RespuestaControl;


public class Proxy {
	
	static ArrayList<Pair<ObjectId, ArrayList<Socket>>> portSockets = new ArrayList<Pair<ObjectId, ArrayList<Socket>>>();
	ServerSocket proxy;
	private static int proxyPort = 3338;
	private static MongoCollection<Document> servers;
	private static Hashtable<ObjectId, Integer> servers_hash = new Hashtable<ObjectId, Integer>();
	private static PeticionControl p;
	
	public Proxy(){				
        try {
        	this.init();
        	
        	this.proxy = new ServerSocket(proxyPort);
    		System.out.println("Arrancando el servidor proxy...");       
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
	public static void main(String[] args) {
		new Proxy();
	}
	
	//---------------------------------------//
	
	
	
	public void init() {
		servers = ProxyConnection.setProxyMongoDBConnection();
		FindIterable<Document> iterable = servers.find();
        MongoCursor<Document> cursor = iterable.iterator();
        while (cursor.hasNext()) {
        	ObjectId id = (ObjectId)cursor.next().get("_id");
        	if(id != null) {
        		servers_hash.put(id,0);
        		portSockets.add(new Pair(id,new ArrayList<Socket>()));
        	}
        }
	}
	public static Entry<ObjectId, Integer> getFewerConnectionsServer(Hashtable<ObjectId, Integer> t){

	       //Transfer as List and sort it
	       ArrayList<Map.Entry<ObjectId, Integer>> l = new ArrayList(t.entrySet());
	       Collections.sort(l, new Comparator<Map.Entry<ObjectId, Integer>>(){

	         public int compare(Map.Entry<ObjectId, Integer> o1, Map.Entry<ObjectId, Integer> o2) {
	            return o1.getValue().compareTo(o2.getValue());
	        }});
	       
	       t.put((ObjectId)l.get(0).getKey(), l.get(0).getValue());

	       return l.get(0);
	    }
	
	//---------------------------------------//
	
	private static class ClientHandler extends Thread { 
			
	    private final Socket clientSocket; 
	    private Socket proxySocket;
	    private ObjectInputStream client_is, proxy_is;
	    private ObjectOutputStream client_os, proxy_os;
	    
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
	        } catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	        
	    }

	    public void procesaCliente(Socket sServicio) throws UnknownHostException, IOException, ClassNotFoundException {
	        try {           
	        	// proxy actua como cliente** ante los servidores
	        	
	        	this.client_is = new ObjectInputStream( clientSocket.getInputStream() );
	        	this.client_os = new ObjectOutputStream( clientSocket.getOutputStream() );
	        	
	        	//Asignar servidor
	        	
	        	ObjectId _id = getFewerConnectionsServer(servers_hash).getKey();
	        	Bson filter_server = (eq("_id", _id));
	        	Document exists = servers.find(filter_server).first();
	        	
//	        	if(exists != null) {	        		
//	        		System.out.println("SERVER FOUND");
//	        		this.proxySocket = new Socket(exists.getString("ip"), exists.getInteger("port")); //creamos comunciacion con servidor aleatoria --cambiar
//	                servers_hash.put(_id, servers_hash.get(_id)+1);
//	        		System.out.println("numero de conexiones del servidor: " + exists.getInteger("port") + " es: " +  servers_hash.get(_id));
//	                
//	                //Asociamos los objetos al socket
//		        	
//	        		this.proxy_os = new ObjectOutputStream( proxySocket.getOutputStream() );
//		        	this.proxy_is = new ObjectInputStream( proxySocket.getInputStream() );
//			       
//	        		this.proxy_os.writeObject(this.client_is.readObject()); // el proxy le manda al servidor lo que dice el cliente	        		
//		        	this.client_os.writeObject(this.proxy_is.readObject()); // el proxy le manda al cliente lo que le dice el servidor
//		        	servers_hash.put(_id, servers_hash.get(_id)-1);
//	        	}else {
//	        		System.out.println("No cuento con servidores disponibles");
//	        	}   
	        	if(exists == null) {
	        		System.out.println("No cuento con servidores disponibles");
	        	}
	        	else{
	        		System.out.println("SERVER FOUND");
	        		this.proxySocket = new Socket(exists.getString("ip"), exists.getInteger("port")); //creamos comunciacion con servidor aleatoria --cambiar
	                servers_hash.put(_id, servers_hash.get(_id)+1);
	        		System.out.println("numero de conexiones del servidor: " + exists.getInteger("port") + " es: " +  servers_hash.get(_id));
	        		this.proxy_os = new ObjectOutputStream( proxySocket.getOutputStream() );
		        	this.proxy_is = new ObjectInputStream( proxySocket.getInputStream() );
		        	
	        		while(exists != null) {	        				                
		                //Asociamos los objetos al socket
			        	p = (PeticionControl) this.client_is.readObject();
			        	
		        		this.proxy_os.writeObject(p); // el proxy le manda al servidor lo que dice el cliente	        		
			        	
		        		if(!(p.getSubtipo().equals("OP_LOGOUT"))) {
		        			this.client_os.writeObject(this.proxy_is.readObject()); // el proxy le manda al cliente lo que le dice el servidor 
		        			
		        		}else { 		        			
		        			servers_hash.put(_id, servers_hash.get(_id)-1);
		        			System.out.println("numero de conexiones del servidor: " + _id + " es: " +  servers_hash.get(_id));
		        			exists = null;		
		        		}
	        		}
	        		
	        	}   
		        
	        } catch (EOFException ex){
            	System.out.println("Broken Pipe! Redirecting...");
            	System.out.println("SERVER FOUND");
        		this.proxySocket = new Socket("localhost",3341);
        		System.out.println("Redirecting to ('localhost', 3341)");
                
                //Asociamos los objetos al socket
	        	
        		this.proxy_os = new ObjectOutputStream( proxySocket.getOutputStream() );
	        	this.proxy_is = new ObjectInputStream( proxySocket.getInputStream() );

        		this.proxy_os.writeObject(p); // el proxy le manda al servidor lo que dice el cliente	 
	        	this.client_os.writeObject(this.proxy_is.readObject()); // el proxy le manda al cliente lo que le dice el servidor
	        	
            } catch(IOException | ClassNotFoundException e){
	        	e.printStackTrace();
			}finally {
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
