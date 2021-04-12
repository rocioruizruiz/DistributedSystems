package client;

import java.io.*;
import java.net.*;
import protocol.*;


public class Client {

    public static final String version = "1.0";

    private Consola console;

    private Socket s;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private static int PROXY = 3338;

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
            this.os.writeObject(pd);
            // Recibimos la respuesta de control del servidor (objeto serializado)
            RespuestaControl rc = (RespuestaControl)this.is.readObject();
            
            if( rc.getSubtipo().compareTo("OK")==0 ) {
                this.console.writeMessage("Desencriptacion correcta");
            }else if(rc.getSubtipo().compareTo("NOT_OK") == 0) {
                this.console.writeMessage("NO ha llegado una desencriptacion valida...");                
            }
        } catch (ClassNotFoundException ex) {
        	ex.printStackTrace();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
    }
    
    private void doRanking() {
        try {
            // Creamos una peticion de control
            PeticionDatos pd = new PeticionDatos("OP_RANKING");
                        
            //Enviamos el objeto serializado
            this.os.writeObject(pd);
            //Recibimos la respuesta de control del servidor (objeto serializado)
            RespuestaControl rc = (RespuestaControl)this.is.readObject();
            
            if( rc.getSubtipo().compareTo("OK")==0 ) {
            	this.console.writeMessage("RANKING:");
            	System.out.println(rc.getArgs().get(0));
                
            }else if(rc.getSubtipo().compareTo("NOT_OK") == 0) {
                this.console.writeMessage("NO ha llegado un ranking valida...");                
            }
        } catch (ClassNotFoundException ex) {
        	ex.printStackTrace();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
    }
}