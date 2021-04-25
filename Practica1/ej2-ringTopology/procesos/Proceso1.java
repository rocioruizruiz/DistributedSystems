package procesos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import protocol.PeticionDatos;

public class Proceso1 {

	private int puertoIzquierda = 5001;
	private int puertoDerecha = 5002;

	public static void main(String[] args) {
		new Proceso1();
	}

	public Proceso1() {
		while (true) {
			try {
				ServerSocket socketIzquierda = new ServerSocket(puertoIzquierda);
				Socket sIzquierda;
				while ((sIzquierda = socketIzquierda.accept()) != null) {
					// Me acaba de llegar el testigo
					System.out.println("Aceptada conexion de " + sIzquierda.getInetAddress().toString());
					ObjectInputStream inputIzquierda = new ObjectInputStream(sIzquierda.getInputStream());
					PeticionDatos pd = (PeticionDatos) inputIzquierda.readObject();
					String mensaje = pd.getSubtipo();
					System.out.println(mensaje);
					// Funcionalidad al activar el nodo de la izquierda (recibir comando)
					boolean done = false;
					if (mensaje.toString().compareTo("OP_ROTATION") == 0) {
						doRotation();
						done = true;
					}
					if (mensaje.toString().compareTo("OP_TRANSLATION") == 0) {
						doTranslation();
						done = true;
					}
					if (mensaje.toString().compareTo("OP_STOP") == 0) {
						doStop();
						done = true;
					}
					if (mensaje.toString().compareTo("OP_STOPALL") == 0) {
						doStopAll();
						done = true;
					}

					if (done) {
						Socket socketDerecha = new Socket("localhost", puertoDerecha);
						ObjectOutputStream outputDerecha = new ObjectOutputStream(socketDerecha.getOutputStream());
						outputDerecha.writeObject(pd);
						if (outputDerecha != null)
							outputDerecha.close();
						if (socketDerecha != null)
							socketDerecha.close();
						// Importante, cerrar el socket izquierda porque lo voy a volver a abrir
						if (inputIzquierda != null)
							inputIzquierda.close();
						if (sIzquierda != null)
							sIzquierda.close();
						if (socketIzquierda != null)
							socketIzquierda.close();
					} else {
						System.out.println("Not done");
					}

				}
			
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}

	// --------------------------------------------------------------------------

	public void doStopAll() {
		System.out.println("AQUI HAN PARADO TODOS");
	}

	public void doStop() {
		System.out.println("AQUI HAN PARADO 1");
	}

	public void doTranslation() {
		System.out.println("TRANSLACION");
	}

	public void doRotation() {
		System.out.println("ROTACION");
	}
}
