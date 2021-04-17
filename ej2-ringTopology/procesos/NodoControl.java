package procesos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class NodoControl {

	private static int puertoRecepcion1 = 5011;
	private static int puertoRecepcion2 = 5012;
	private static int puertoEnvio1 = 5000;
	private static int puertoEnvio2 = 5001;

	private static PeticionDatos lastPet = new PeticionDatos();

	public static void main(String[] args) throws IOException {
		while (true) {
			try {
				ServerSocket socketProceso = new ServerSocket(puertoRecepcion2);
				Socket sProceso;
				ServerSocket socketCliente = new ServerSocket(puertoRecepcion1);
				Socket sCliente;
				if ((sProceso = socketProceso.accept()) != null) {
					// Me acaba de llegar el testigo
					System.out.println("Aceptada conexion de " + sProceso.getInetAddress().toString());
					ObjectInputStream inputIzquierda = new ObjectInputStream(sProceso.getInputStream());
					PeticionDatos pd = (PeticionDatos) inputIzquierda.readObject();
					String mensaje = pd.getSubtipo();
					lastPet.setSubtipo(mensaje);
					// Funcionalidad al activar el nodo de la izquierda (recibir comando)

					Socket socketDerecha = new Socket("localhost", puertoEnvio2);
					ObjectOutputStream outputDerecha = new ObjectOutputStream(socketDerecha.getOutputStream());
					outputDerecha.writeObject(pd);
					if (outputDerecha != null)
						outputDerecha.close();
					if (socketDerecha != null)
						socketDerecha.close();
					// Importante, cerrar el socket izquierda porque lo voy a volver a abrir
					if (inputIzquierda != null)
						inputIzquierda.close();
					if (sProceso != null)
						sProceso.close();
					if (socketProceso != null)
						socketProceso.close();

				}

				if ((sCliente = socketCliente.accept()) != null) {
					// Me acaba de llegar el testigo
					System.out.println("Aceptada conexion de " + sCliente.getInetAddress().toString());
					ObjectInputStream inputIzquierda = new ObjectInputStream(sCliente.getInputStream());
					PeticionDatos pd = (PeticionDatos) inputIzquierda.readObject();
					System.out.println(pd.getSubtipo() + " -- " + lastPet.getSubtipo());
					if (pd.getSubtipo().equals(lastPet.getSubtipo())) {
						Socket socketDerecha = new Socket("localhost", puertoEnvio1);
						ObjectOutputStream outputDerecha = new ObjectOutputStream(socketDerecha.getOutputStream());
						RespuestaControl rc = new RespuestaControl("OK");
						System.out.println("OK");
						outputDerecha.writeObject(rc);
						if (outputDerecha != null)
							outputDerecha.close();
						if (socketDerecha != null)
							socketDerecha.close();
						// Importante, cerrar el socket izquierda porque lo voy a volver a abrir
						if (inputIzquierda != null)
							inputIzquierda.close();
						if (sCliente != null)
							sCliente.close();
						if (socketCliente != null)
							socketCliente.close();
					} else {
						Socket socketDerecha = new Socket("localhost", puertoEnvio1);
						ObjectOutputStream outputDerecha = new ObjectOutputStream(socketDerecha.getOutputStream());
						RespuestaControl rc = new RespuestaControl("NOT_OK");
						System.out.println("NOT_OK");
						outputDerecha.writeObject(rc);
						if (outputDerecha != null)
							outputDerecha.close();
						if (socketDerecha != null)
							socketDerecha.close();
						// Importante, cerrar el socket izquierda porque lo voy a volver a abrir
						if (inputIzquierda != null)
							inputIzquierda.close();
						if (sCliente != null)
							sCliente.close();
						if (socketCliente != null)
							socketCliente.close();
					}
				}
			} catch (IOException ex) {
				System.out.println(ex);
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}
}