package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Cliente {

	private static int puertoRecepcion = 5000;
	private static int puertoEnvio = 5012;

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
				outputEnvio.writeObject(p);
				while (!done) {
					socketRecepcion = socketIzquierda.accept();
					ObjectInputStream inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
					RespuestaControl rc = (RespuestaControl) inputRecepcion.readObject();
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
}