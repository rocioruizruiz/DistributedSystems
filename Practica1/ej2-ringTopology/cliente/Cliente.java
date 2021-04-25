package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Cliente {

	private static int puertoRecepcion = 5000;
	private static int puertoEnvio = 5012;
	
    private static ArrayList<Long> latencia_red = new ArrayList<Long>();
    private static ArrayList<Long> latencia_app = new ArrayList<Long>();
    
    private static long average_latencia_red = 0;
    private static long average_latencia_app = 0;

	public static void main(String[] args) throws IOException {
		ServerSocket socketIzquierda = new ServerSocket(puertoRecepcion);
		Socket socketRecepcion;
		System.out.println("Bienvenido a la terminal cliente que controla la cadena de producción\n");
		try {
			while (true) {
				boolean done = false;
				Socket socketEnvio = new Socket("localhost", puertoEnvio);
				ObjectOutputStream outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());
				System.out.println("Que operacion deseas realizar: OP_ROTATION OP_TRANSLATION OP_STOP OP_STOPALL");
				Scanner scan = new Scanner(System.in);
				String command = scan.nextLine();
				PeticionDatos p = new PeticionDatos(command);
				long startTime = System.currentTimeMillis();
	            outputEnvio.writeObject(p);
	            long this_latency = (System.currentTimeMillis()-startTime);
	            latencia_red.add((this_latency));
	            System.out.println("Latencia de red actual: " + this_latency +"ms.");
	            averageNetworkLatency();
				while (!done) {
					socketRecepcion = socketIzquierda.accept();
					ObjectInputStream inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
					RespuestaControl rc = (RespuestaControl) inputRecepcion.readObject();
					this_latency = (System.currentTimeMillis()-startTime);
		            latencia_app.add((this_latency));
		            System.out.println("Tiempo de respuesta actual: " + this_latency +"ms.");
		            averageAppLatency();
					String mensaje = rc.getSubtipo();
					if (mensaje.equals("OK")) {
						done = true;
						System.out.println("La operación se ha completado con exito!");
					}
					if (inputRecepcion != null)
						inputRecepcion.close();
					if (socketRecepcion != null)
						socketRecepcion.close();
				}
			}
		} catch (UnknownHostException ex) {
		} catch (IOException ex) {
			System.out.println(ex);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	private static void averageNetworkLatency() {
    	for(int i=0; i < latencia_red.size(); i++) {
    		average_latencia_red += latencia_red.get(i);	
    	}
    	average_latencia_red = average_latencia_red/latencia_red.size();
    	System.out.println("La latencia de red media es de: " + average_latencia_red + "ms.");
    	average_latencia_red = 0;
    }
    private static void averageAppLatency() {
    	for(int i=0; i < latencia_app.size(); i++) {	
    		average_latencia_app += latencia_app.get(i);	
    	}
    	average_latencia_app = average_latencia_app/latencia_app.size();
    	System.out.println("El tiempo de respuesta medio es de: " + average_latencia_app + "ms.");
    	average_latencia_app = 0;
    }
}