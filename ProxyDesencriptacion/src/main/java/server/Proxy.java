package server;


import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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

import protocol.PeticionDatos;
import protocol.RespuestaControl;


public class Proxy {
	ServerSocket proxy;
	private static int proxyPort = 3338;
	private static MongoCollection<Document> servers;
	private static Hashtable<ObjectId, Integer> servers_hash = new Hashtable<ObjectId, Integer>();
	static Hashtable<String, Integer> servers_ranking = new Hashtable<String, Integer>();
	
	
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
	//---------------------------------------//
	
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
        		Bson filter_server = (eq("_id", id));
	        	Document exists = servers.find(filter_server).first();
        		servers_ranking.put((exists.getInteger("port")+ "-" +exists.getString("ip")),0);
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
	
//	public static ArrayList<Map.Entry<Integer, Integer>> getRanking(Hashtable<Integer, Integer> t){
//
//       //Transfer as List and sort it
//       ArrayList<Map.Entry<Integer, Integer>> l = new ArrayList(t.entrySet());
//       Collections.sort(l, new Comparator<Map.Entry<Integer, Integer>>(){
//         public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
//            return o2.getValue().compareTo(o1.getValue());
//        }});
//       
//       System.out.println(l);
//       return l;
//    }
	public static String getRanking(Hashtable<String, Integer> t){
		String mensaje = "";

       //Transfer as List and sort it
       ArrayList<Map.Entry<String, Integer>> l = new ArrayList(t.entrySet());
       Collections.sort(l, new Comparator<Map.Entry<String, Integer>>(){
         public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }});
       
       for(int i=0; i<l.size(); i++) {
    	   mensaje =  mensaje + (i+1) + ". Servidor: " + l.get(i).getKey() + " numero de mensajes descifrados: " + l.get(i).getValue() + "\n";
       }
       return mensaje;
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
	        } 
	        
	    }

	    public void procesaCliente(Socket sServicio) {
	        try {           
	        	// proxy actua como cliente** ante los servidores
	        	
	        	this.client_is = new ObjectInputStream( clientSocket.getInputStream() );
	        	this.client_os = new ObjectOutputStream( clientSocket.getOutputStream() );
	        	
	        	PeticionDatos peticionCliente = (PeticionDatos)this.client_is.readObject();
	        	if(peticionCliente.getSubtipo().equals("OP_RANKING")) {
	        		System.out.println("Generando rankinggg!!");
	            	RespuestaControl rc = new RespuestaControl("OK");
	            	rc.getArgs().add(getRanking(servers_ranking));
	                this.client_os.writeObject(rc);              	
        		}else {
        			ObjectId _id = getFewerConnectionsServer(servers_hash).getKey();  	
    	        	Bson filter_server = (eq("_id", _id));
    	        	Document exists = servers.find(filter_server).first();
        			if(exists != null) {	        		
    	        		System.out.println("SERVER FOUND");
    	        		this.proxySocket = new Socket(exists.getString("ip"), exists.getInteger("port")); //creamos comunciacion con servidor aleatoria --cambiar
    	                servers_hash.put(_id, servers_hash.get(_id)+1);
    	        		System.out.println("numero de conexiones del servidor: " + exists.getInteger("port") + " es: " +  servers_hash.get(_id));
    	                
    	                //Asociamos los objetos al socket
    		        	
    	        		this.proxy_os = new ObjectOutputStream( proxySocket.getOutputStream() );
    		        	this.proxy_is = new ObjectInputStream( proxySocket.getInputStream() );

    	        		this.proxy_os.writeObject(peticionCliente); // el proxy le manda al servidor lo que dice el cliente	 
    		        	this.client_os.writeObject(this.proxy_is.readObject()); // el proxy le manda al cliente lo que le dice el servidor
    		        	
    		        	servers_hash.put(_id, servers_hash.get(_id)-1);
    		        	servers_ranking.put((exists.getInteger("port")+ "-" +exists.getString("ip")), servers_ranking.get((exists.getInteger("port")+ "-" +exists.getString("ip")))+1);
    		        	
    	        	} else {
    	        		System.out.println("No cuento con servidores disponibles");
    	        	}   
        			
        		}
	        	
	        	//Asignar servidor
	        	
	        	
	        	
	        	
	        	  	
		        
	        }catch(IOException | ClassNotFoundException e){
	        	
	        }finally {
	            try {
	                if(proxy_os != null) proxy_os.close();
	                if(proxy_is != null)proxy_is.close();
	                if(sServicio != null)sServicio.close();
	            } catch (IOException ex) {
	            }
	        }
	    }
	}
}
