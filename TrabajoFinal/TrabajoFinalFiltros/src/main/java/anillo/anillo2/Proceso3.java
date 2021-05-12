package anillo.anillo2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import org.omg.CORBA.ORB;

import FilterApp.Filter;
import FilterApp.FilterHelper;
import protocol.PeticionDatos;

public class Proceso3 {

	private int puertoIzquierda = 5014;
	private int puertoDerecha = 5015;
	private String token = "";

	public static void main(String[] args) {
		new Proceso3();
	}

	public Proceso3() {
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
						System.out.println("My token: " + this.token);
						if (pd.getNodoanillo().equals(this.token)) {
							doFiltro(pd.getFiltro(), pd.getPath());
							pd.setPath("/home/agus/Documents/");
							done = true;
						} else {
							System.out.println("OP not sent to my token");
							done = true;
						}
					}

					if (done) {
						System.out.println("1");
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
		double sysLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()
				+ (new Random().nextDouble());
		System.out.println("Mi carga de CPU es de: " + sysLoad);
		return sysLoad;
	}

	public void doFiltro(String filter, String path) {
		String[] args = null;
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// Finds the object "hello" corba object associated with the servant previously
			// attached to the naming Service
			org.omg.CORBA.Object objRef = orb.string_to_object("corbaname::localhost:1050#Filter");

			// resolve the Object Reference in Naming
			Filter filterImpl = FilterHelper.narrow(objRef);

			System.out.println("Obtained a handle on server object: " + filterImpl);
			filterImpl.applyFilter(filter, path);

		} catch (Exception e) {
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
