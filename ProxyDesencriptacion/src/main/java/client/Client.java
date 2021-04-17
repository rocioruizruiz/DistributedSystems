package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import protocol.*;


public class Client {

    public static final String version = "1.0";

    private Consola console;

    private Socket s;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private static int PROXY = 3338;
    private ArrayList<Long> latencia_red_desencriptacion = new ArrayList<Long>();
    private ArrayList<Long> latencia_app_desencriptacion = new ArrayList<Long>();
    private ArrayList<Long> latencia_red_ranking = new ArrayList<Long>();
    private ArrayList<Long> latencia_app_ranking = new ArrayList<Long>();
    private long average_latencia_red_desencriptacion = 0;
    private long average_latencia_app_desencriptacion = 0;
    private long average_latencia_red_ranking = 0;
    private long average_latencia_app_ranking = 0;

    //--------------------------------------------------------------------------


    public static void main(String[] args) {
        new Client();
    }

    //--------------------------------------------------------------------------

    private void init() {
        this.console = new Consola();
    }
    
    //--------------------------------------------------------------------------
    
    public Client() {
        
        this.init();
        
        String cmd = this.console.getCommand();
        while( cmd.compareTo("close")!=0 ) {
            if( cmd.compareTo("Desencriptar mensaje")==0 ) {
                //Capturamos el mensaje de la consola a desencriptar
                String mensaje = this.console.getCommandMENSAJE();
                System.out.println(mensaje);
                //Creamos el socket en el puerto 3338 del hostlocal y conectamos
                // los objetos de entrada y salida para serializar al socket
                this.doConnect(PROXY);
                
                //Enviamos las credenciales al servidor
                this.doDesencriptacion(mensaje);
                this.doDisconnect();
            }   
            if( cmd.compareTo("Ranking de servidores")==0 ) {
                //Creamos el socket en el puerto 3338 del hostlocal y conectamos
                // los objetos de entrada y salida para serializar al socket
                this.doConnect(PROXY);
                
                //Enviamos las credenciales al servidor
                this.doRanking();
                this.doDisconnect();
            }   
            
            if( cmd.compareTo("Balanceo de carga") == 0 ) {
                //Creamos el socket en el puerto 3338 del hostlocal y conectamos
                // los objetos de entrada y salida para serializar al socket
                this.doConnect(PROXY);
                
                //Enviamos las credenciales al servidor
                this.doBalanceo();
                this.doDisconnect();
            }   
            
            cmd = this.console.getCommand();
        }
        if( this.s!=null )
            this.doDisconnect();
        
        this.console.writeMessage("Saliendo de la aplicacion");
    }

    //--------------------------------------------------------------------------

    private void doConnect(int port) {
        try {
            //if(this.s == null){
                //Creamos el socket
                this.s = new Socket("localhost", port);
                //Asociamos los objetos al socket
                this.os = new ObjectOutputStream( s.getOutputStream() );
                this.is = new ObjectInputStream( s.getInputStream() );
            //}
        } catch (UnknownHostException ex) {
        	ex.printStackTrace();
        } catch (EOFException ex){
        	System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!");
        } catch (IOException ex) {
        	ex.printStackTrace();
        }        
    }
    //--------------------------------------------------------------------------

    private void doDisconnect() {
        if (this.s!=null){
            try {
                // Creamos una peticion de control, la serializamos y la mandamos
                PeticionControl p = new PeticionControl("OP_LOGOUT");
                this.os.writeObject(p);
                this.is.close();
                this.is = null;
                this.os.close();
                this.os = null;
                this.s.close();
                this.s = null;
            } catch (IOException ex) {
            	
            }
        }else{
            System.out.println("Ya estabas desconectado");
        }
    }
    
    //--------------------------------------------------------------------------
    //CREAR: doResgister
    private void doDesencriptacion(String credenciales) {
        try {
            // Creamos una peticion de control
            PeticionDatos pd = new PeticionDatos("OP_DESENCRIPTACION");
            pd.getArgs().add(credenciales);
            
            //Enviamos el objeto serializado
            long startTime = System.currentTimeMillis();
            this.os.writeObject(pd);
            long this_latency = (System.currentTimeMillis()-startTime);
            latencia_red_desencriptacion.add((this_latency));
            System.out.println("Latencia de red actual: " + this_latency +"ms.");
            averageNetworkLatency();
            // Recibimos la respuesta de control del servidor (objeto serializado)
            RespuestaControl rc = (RespuestaControl)this.is.readObject();
            this_latency = (System.currentTimeMillis()-startTime);
            latencia_app_desencriptacion.add((this_latency));
            System.out.println("Tiempo de respuesta actual: " + this_latency +"ms.");
            averageAppLatency();
            if( rc.getSubtipo().compareTo("OK")==0 ) {
                this.console.writeMessage("Desencriptacion correcta");
            }else if(rc.getSubtipo().compareTo("NOT_OK") == 0) {
                this.console.writeMessage("NO ha llegado una desencriptacion valida...");                
            }
        } catch (ClassNotFoundException ex) {
        	ex.printStackTrace();
        } catch (EOFException ex){
        	System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!");
        } catch (IOException ex) {
        	
        } 
    }
    
    private void doRanking() {
        try {
            // Creamos una peticion de control       	
            PeticionDatos pd = new PeticionDatos("OP_RANKING");
                        
            //Enviamos el objeto serializado
            long startTime = System.currentTimeMillis();
            this.os.writeObject(pd);
            long this_latency = (System.currentTimeMillis()-startTime);
            latencia_red_ranking.add((this_latency));
            System.out.println("Latencia de red actual: " + this_latency +"ms.");
            averageNetworkLatencyRanking();
            //Recibimos la respuesta de control del servidor (objeto serializado)
            RespuestaControl rc = (RespuestaControl)this.is.readObject();
            
            this_latency = (System.currentTimeMillis()-startTime);
            latencia_app_ranking.add((this_latency));
            System.out.println("Tiempo de respuesta actual: " + this_latency +"ms.");
            averageAppLatencyRanking();
            
            if( rc.getSubtipo().compareTo("OK")==0 ) {
            	this.console.writeMessage("RANKING:");
            	System.out.println(rc.getArgs().get(0));
                
            }else if(rc.getSubtipo().compareTo("NOT_OK") == 0) {
                this.console.writeMessage("No ha llegado un ranking valido...");                
            }
        } catch (ClassNotFoundException ex) {
        	ex.printStackTrace();
        } catch (EOFException ex){
        	System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!!");
        }catch (IOException ex) {
        	ex.printStackTrace();
        } 
    }
    
    private void doBalanceo() {
        try {
            // Creamos una peticion de control       	
            PeticionDatos pd = new PeticionDatos("OP_BALANCEO");
                        
            //Enviamos el objeto serializado
            long startTime = System.currentTimeMillis();
            this.os.writeObject(pd);
            long this_latency = (System.currentTimeMillis()-startTime);
            latencia_red_ranking.add((this_latency));
            System.out.println("Latencia de red actual: " + this_latency +"ms.");
            averageNetworkLatencyRanking();
            //Recibimos la respuesta de control del servidor (objeto serializado)
            RespuestaControl rc = (RespuestaControl)this.is.readObject();
            
            this_latency = (System.currentTimeMillis()-startTime);
            latencia_app_ranking.add((this_latency));
            System.out.println("Tiempo de respuesta actual: " + this_latency +"ms.");
            averageAppLatencyRanking();
            
            if( rc.getSubtipo().compareTo("OK")==0 ) {
            	this.console.writeMessage("RANKING:");
            	System.out.println(rc.getArgs().get(0));
                
            }else if(rc.getSubtipo().compareTo("NOT_OK") == 0) {
                this.console.writeMessage("No ha llegado un ranking valido...");                
            }
        } catch (ClassNotFoundException ex) {
        	ex.printStackTrace();
        } catch (EOFException ex){
        	System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!!");
        }catch (IOException ex) {
        	ex.printStackTrace();
        } 
    }
    
    
    private void averageNetworkLatency() {
    	for(int i=0; i < latencia_red_desencriptacion.size(); i++) {
    		average_latencia_red_desencriptacion += latencia_red_desencriptacion.get(i);	
    	}
    	average_latencia_red_desencriptacion = average_latencia_red_desencriptacion/latencia_red_desencriptacion.size();
    	System.out.println("La latencia de red media es de: " + average_latencia_red_desencriptacion + "ms.");
    	average_latencia_red_desencriptacion = 0;
    }
    private void averageAppLatency() {
    	for(int i=0; i < latencia_app_desencriptacion.size(); i++) {	
    		average_latencia_app_desencriptacion = average_latencia_app_desencriptacion + latencia_app_desencriptacion.get(i);	
    	}
    	average_latencia_app_desencriptacion = average_latencia_app_desencriptacion/latencia_app_desencriptacion.size();
    	System.out.println("El tiempo de respuesta medio es de: " + average_latencia_app_desencriptacion + "ms.");
    	average_latencia_app_desencriptacion = 0;
    }
    // --------- RANKING ------------
    private void averageNetworkLatencyRanking() {
    	for(int i=0; i < latencia_red_ranking.size(); i++) {
    		average_latencia_red_ranking += latencia_red_ranking.get(i);	
    	}
    	average_latencia_red_ranking = average_latencia_red_ranking/latencia_red_ranking.size();
    	System.out.println("La latencia de red media es de: " + average_latencia_red_ranking + "ms.");
    	average_latencia_red_ranking = 0;
    }
    private void averageAppLatencyRanking() {
    	for(int i=0; i < latencia_app_ranking.size(); i++) {	
    		average_latencia_app_ranking = average_latencia_app_ranking + latencia_app_ranking.get(i);	
    	}
    	average_latencia_app_ranking = average_latencia_app_ranking/latencia_app_ranking.size();
    	System.out.println("El tiempo de respuesta medio es de: " + average_latencia_app_ranking + "ms.");
    	average_latencia_app_ranking = 0;
    }
}