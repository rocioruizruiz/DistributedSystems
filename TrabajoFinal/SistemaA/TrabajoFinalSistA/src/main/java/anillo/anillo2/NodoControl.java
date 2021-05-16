package anillo.anillo2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class NodoControl {

	private static int puertoEscuchaServer = 5011;
	private static int puertoEscuchaProceso3 = 5015;

	private static int puertoEnvioAServer = 5010;
	private static int puertoEnvioAProceso1 = 5012;

	private static PeticionDatos lastPet = new PeticionDatos();

	private static final Logger LOGGER = LogManager.getLogger(NodoControl.class);
	private static String NODES = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/TrabajoFinalSistA/src/main/resources/nodes.txt";
	private static String id = "8";

	public static void main(String[] args) throws IOException {
		while (true) {
			try {
				init();
				
				ServerSocket socketProceso = new ServerSocket(puertoEscuchaServer);
				Socket sProceso;
				ServerSocket socketCliente = new ServerSocket(puertoEscuchaProceso3);
				Socket sCliente;

				System.out.println("Arrancando el NodoControl en el puerto " + puertoEscuchaServer + "...");
				System.out.println("Arrancando el NodoControl en el puerto " + puertoEscuchaProceso3 + "...");
				if ((sProceso = socketProceso.accept()) != null) { // Me llega del server2 y paso peticion a proceso 1
					// Me acaba de llegar el testigo
					LOGGER.info(
							"Nodo control de Anillo 2 ha aceptado la conexión " + sProceso.getInetAddress().toString());
					System.out.println("Aceptada conexion de " + sProceso.getInetAddress().toString());
					ObjectInputStream inputIzquierda = new ObjectInputStream(sProceso.getInputStream());
					PeticionDatos pd = (PeticionDatos) inputIzquierda.readObject();
					String mensaje = pd.getSubtipo();
					lastPet.setSubtipo(mensaje);

					// Funcionalidad al activar el nodo de la izquierda (recibir comando)
					Socket socketDerecha = new Socket("localhost", puertoEnvioAProceso1);
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

				if ((sCliente = socketCliente.accept()) != null) { // Me llega del proceso 3 y paso respuesta a server2
					// Me acaba de llegar el testigo
					LOGGER.info(
							"Nodo control de Anillo 2 ha aceptado la conexión " + sProceso.getInetAddress().toString());
					System.out.println("Aceptada conexion de " + sCliente.getInetAddress().toString());
					ObjectInputStream inputIzquierda = new ObjectInputStream(sCliente.getInputStream());
					PeticionDatos pd = (PeticionDatos) inputIzquierda.readObject();
					System.out.println(pd.getSubtipo() + " -- " + lastPet.getSubtipo());
					if (pd.getSubtipo().equals(lastPet.getSubtipo())) {
						Socket socketDerecha = new Socket("localhost", puertoEnvioAServer);
						ObjectOutputStream outputDerecha = new ObjectOutputStream(socketDerecha.getOutputStream());
						RespuestaControl rc = new RespuestaControl("OK");
						if (pd.getSubtipo().compareTo("OP_CPU") == 0) {
							for(int i = 0; i < pd.getCpus().size(); i++) {
								System.out.println(" > Nodo de token: " + pd.getTokens().get(i) + " - CPU: " + pd.getCpus().get(i));
							}
							rc.setCpus(pd.getCpus());
							rc.setTokens(pd.getTokens());
							System.out.println("OK");
							outputDerecha.writeObject(rc);
						}
						if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) {
							if (!pd.getPath().contentEquals("NOT_OK")) {
								System.out.println("Ruta final: " +  pd.getPath());
								System.out.println("OK");
								rc.setPath(pd.getPath());
							} else {
								rc.setSubtipo("NOT_OK");
								System.out.println("NOT_OK");
							}
							outputDerecha.writeObject(rc);
						}
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
						Socket socketDerecha = new Socket("localhost", puertoEnvioAServer);
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
				LOGGER.error("I/O error while executing thread", ex);
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				LOGGER.error("Class not found error while executing thread", ex);
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void init() {
		try {
			File file = new File(NODES);
			String last = "";
			BufferedReader br = new BufferedReader(new FileReader(file));
			last = br.readLine();
			if (file.exists()) {
				while (last != null) {
					String[] address = last.split(";");
					if (address[0].equals(NodoControl.getId())) {
						puertoEscuchaServer = Integer.parseInt(address[1]);
						puertoEscuchaProceso3 = Integer.parseInt(address[2]);
						break;
					}
					last = br.readLine();
				}

			}
			br.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("No se ha encontrado el archivo nodes.txt");
		} catch (IOException e) {
			LOGGER.error("Error al cargar el archivo nodes.txt");
		}
	}

	public static String getId() {
		return id;
	}

	public static void setId(String id) {
		NodoControl.id = id;
	}
}