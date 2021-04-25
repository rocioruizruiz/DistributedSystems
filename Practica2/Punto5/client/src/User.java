import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import protocol.PeticionControl;
import protocol.RespuestaControl;

public class User {
	private Socket s;
    private ObjectOutputStream os;
    private ObjectInputStream is;
	private static int PORT = 3338;
	private static String IP = "localhost";
	
    private ArrayList<Long> latencia_red = new ArrayList<Long>();
    private ArrayList<Long> latencia_app = new ArrayList<Long>();
    private long average_latencia_red = 0;
    private long average_latencia_app = 0;

	
	public User() {
		boolean finish = false;
		
		while( !finish ) {
	
			Scanner sc = new Scanner(System.in);
			System.out.println("Inserte el PATH de la imagen a filtrar");
			String PATH = sc.nextLine();
			ArrayList<String> filters = new ArrayList<String>();
			boolean done = false;
			while(!done) {
				System.out.println("Inserte el filtro a aplicar");
				filters.add(sc.nextLine());
				System.out.println("Si quieres añadir otro filtro? (y/n)");
				if(sc.nextLine().toLowerCase().equals("n")) done = true;
			}
			System.out.println("Enviando solicitud para filtrar la imagen: " + PATH);

            //Creamos el socket en el puerto 3338 del hostlocal y conectamos
            // los objetos de entrada y salida para serializar al socket
            this.doConnect(PORT, IP);
            
            //Enviamos las credenciales al servidor
            this.doFiltering(PATH, filters);
            this.doDisconnect();
            
            System.out.println("Quieres filtrar otra imagen? (y/n)");
			if(sc.nextLine().toLowerCase().equals("n")) { finish = true; sc.close(); }
            
        }   
	}
private void doConnect(int port, String ip) {
    try {
        //if(this.s == null){
            //Creamos el socket
            this.s = new Socket(ip, port);
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
	private void doFiltering(String path, ArrayList<String> filters) {
	    try {
	        // Creamos una peticion de control
	        PeticionControl pd = new PeticionControl("OP_FILTER", path);
	        pd.setArgs(filters);
	        
	        //Enviamos el objeto serializado
	        long startTime = System.currentTimeMillis();
	        this.os.writeObject(pd);
	        long this_latency = (System.currentTimeMillis()-startTime);
	        latencia_red.add((this_latency));
	        System.out.println("Latencia de red actual: " + this_latency +"ms.");
	        averageNetworkLatency();
	        // Recibimos la respuesta de control del servidor (objeto serializado)
	        RespuestaControl rc = (RespuestaControl)this.is.readObject();
	        this_latency = (System.currentTimeMillis()-startTime);
	        latencia_app.add((this_latency));
	        System.out.println("Tiempo de respuesta actual: " + this_latency +"ms.");
	        averageAppLatency();
	        if( rc.getSubtipo().compareTo("OK")==0 ) {
	            System.out.println("La imagen filtrada se encuentra en: " + rc.getPath());
	        }else {
	        	System.out.println("el filtrado no se ha realizado correctamente");
	        }
	    } catch (ClassNotFoundException ex) {
	    	ex.printStackTrace();
	    } catch (EOFException ex){
	    	System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!");
	    } catch (IOException ex) {
	    	
	    } 
	}
	
	private void averageNetworkLatency() {
    	for(int i=0; i < latencia_red.size(); i++) {
    		average_latencia_red += latencia_red.get(i);	
    	}
    	average_latencia_red = average_latencia_red/latencia_red.size();
    	System.out.println("La latencia de red media es de: " + average_latencia_red + "ms.");
    	average_latencia_red = 0;
    }
    private void averageAppLatency() {
    	for(int i=0; i < latencia_app.size(); i++) {	
    		average_latencia_app = average_latencia_app + latencia_app.get(i);	
    	}
    	average_latencia_app = average_latencia_app/latencia_app.size();
    	System.out.println("El tiempo de respuesta medio es de: " + average_latencia_app + "ms.");
    	average_latencia_app = 0;
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new User();

	}

}
