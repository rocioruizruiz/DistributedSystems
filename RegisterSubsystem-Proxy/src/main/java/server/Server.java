package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


import com.mongodb.client.MongoDatabase;
import com.mongodb.conection.*;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import org.bson.conversions.Bson;

import protocol.*;

public class Server {

    ServerSocket s;
    int port;
    private static List<MongoDatabase> databases = new ArrayList<MongoDatabase>();
    
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
    	new ServerConnection(databases);
    	

    }


    
    private static class ClientHandler extends Thread { 
		
	    private final Socket clientSocket; 
	    private ObjectInputStream is;
	    private ObjectOutputStream os;
	    private Usuario usuario;
	    
	    // Constructor 
	    public ClientHandler(Socket socket) 
	    { 
	        this.clientSocket = socket; 
	    } 
	
	    public void run() 
	    { 
	        this.init();
	        try {               
	             // start 
	            procesaCliente(clientSocket);
	            
	        }catch (Error e) { 
	            e.printStackTrace(); 
	        } 
	        
	    }
	    //--------------------------------------------------------------------------

	    public void init() {
	        
	        // Codigo de inicializacion ...
	        // fileUsers = new FileUsers();
	      
	        
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
	                if( p.getTipo().compareTo("PETICION_CONTROL")==0 ) {
	                    PeticionControl pc = (PeticionControl)p;
	                    if( pc.getSubtipo().compareTo("OP_LOGIN")==0 )
	                        this.doLogin(pc);
	                    if( pc.getSubtipo().compareTo("OP_LOGOUT")==0 ) {
	                        this.doLogout();
	                        end = true;
	                    }
	                    
	                    if( pc.getSubtipo().compareTo("OP_REGISTER")==0 ) {
	                        this.doRegister(pc);                        
	                    }
	                    
	                     
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

	    public void doLogin(PeticionControl pc) {
	        
	        String login = (String) pc.getArgs().get(0);
	        String password = (String) pc.getArgs().get(1);
	        try {
	            
	            Bson filter_login = (eq("login", login));
	            Bson filter = and((eq("login", login)), (eq("password", password)));
	            Document exists = databases.get(0).getCollection("Users").find(filter).first();
	            Document exists_login = databases.get(0).getCollection("Users").find(filter_login).first();
	            
	            if( exists != null ) {
	            	this.usuario = new Usuario(login, password);
	                RespuestaControl rc = new RespuestaControl("OP_LOGIN_OK");
	                this.os.writeObject(rc);
	                System.out.println("Usuario " + this.usuario.getLogin() + ": conectado");
	                
	            }
	            else if( exists_login != null ) {
	                RespuestaControl rc = new RespuestaControl("OP_LOGIN_BAD_PASSWORD");
	                this.os.writeObject(rc);
	                System.out.println("Intento de acceso con contraseña incorrecta");
	                this.usuario = null;
	            }
	            else  {
	                RespuestaControl rc = new RespuestaControl("OP_LOGIN_NO_USER");
	                this.os.writeObject(rc);
	                System.out.println("Intento de acceso con login incorrecto");
	                this.usuario = null;
	            }
	        } catch (Exception ex) {
	        }
	        
	    }
	    //--------------------------------------------------------------------------
	    //CREAR: Metodo para registro de usuarios
	    public void doRegister(PeticionControl pc) {
	        
	        String login = (String) pc.getArgs().get(0);
	        String password = (String) pc.getArgs().get(1);
	        try {
	        	Bson filter_login = (eq("login", login));
	            Document exists_login = databases.get(0).getCollection("Users").find(filter_login).first();
	            
	            if( exists_login != null ) {
	                RespuestaControl rc = new RespuestaControl("OP_REG_NOK");
	                this.os.writeObject(rc);
	                System.out.println("Usuario " + login + " no registrado porque ya existe");

	            }else {
	                RespuestaControl rc = new RespuestaControl("OP_REG_OK");
	                sleep(25000);
	                this.os.writeObject(rc);
	                System.out.println("Nuevo usuario, registrando usuario...");
	                databases.get(0).getCollection("Users").insertOne(new Document("login", login).append("password", password));
	                System.out.println("Usuario registrado satisfactoriamente");
	                this.usuario = null;
	            }
	        } catch (SocketException ex) {
	        }catch (Exception ex) {
	        	ex.printStackTrace();
	        }
	        
	    }

	    //--------------------------------------------------------------------------

	    public void doLogout() {
	        if( this.usuario != null ){
	            System.out.println("Desconectado usuario " + this.usuario.getLogin());
	            this.usuario = null;
	        }
	    }
	} 
}
