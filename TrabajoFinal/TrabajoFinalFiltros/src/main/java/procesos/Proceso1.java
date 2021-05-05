package procesos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.UUID;

import protocol.PeticionDatos;
import server.Pair;

public class Proceso1 {

	private int puertoIzquierda = 5001;
	private int puertoDerecha = 5002;
	private String token = "";

	public static void main(String[] args) {
		new Proceso1();
	}

	public Proceso1() {
		this.setToken(UUID.randomUUID().toString());
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
					if (mensaje.toString().compareTo("OP_CPU") == 0) {
						double sysLoad = doCPU();
						
						pd.getCpus().add(sysLoad);
						pd.getTokens().add(this.token);
						System.out.println(pd.getTokens() + " - " + pd.getCpus());
						done = true;
					}
					if (mensaje.toString().compareTo("OP_FILTRO") == 0) {
						doFiltro();
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
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}

	// --------------------------------------------------------------------------

	public double doCPU() {
		double sysLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() + (new Random().nextDouble());
		System.out.println("Mi carga de CPU es de: " + sysLoad);
		return sysLoad;
		
	}

	public void doFiltro() {
		System.out.println("OP FILTRO");
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
