package anillo.anillo2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.ORB;

import FilterApp.Filter;
import FilterApp.FilterHelper;
import protocol.PeticionDatos;

public class Proceso1 {

	private int puertoIzquierda = 5012;
	private int puertoDerecha = 5013;
	private String token = "";

	private static final Logger LOGGER = LogManager.getLogger(Proceso1.class);
	private String NODES = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/TrabajoFinalSistA/src/main/resources/nodes.txt";
	private static String id = "13";

	public static void main(String[] args) {
		new Proceso1();
	}

	public Proceso1() {
		this.setToken(UUID.randomUUID().toString());
		this.init();
		while (true) {
			try {

				System.out.println("Arrancando el proceso 1 del anillo 2 en el puerto " + puertoIzquierda + "...");
				ServerSocket socketIzquierda = new ServerSocket(puertoIzquierda);
				Socket sIzquierda;
				if ((sIzquierda = socketIzquierda.accept()) != null) {
					// Me acaba de llegar el testigo
					LOGGER.info(
							"Proceso 1 de Anillo 2 ha aceptado la conexión " + sIzquierda.getInetAddress().toString());
					System.out.println("Aceptada conexion de " + sIzquierda.getInetAddress().toString());
					ObjectInputStream inputIzquierda = new ObjectInputStream(sIzquierda.getInputStream());
					PeticionDatos pd = (PeticionDatos) inputIzquierda.readObject();
					String mensaje = pd.getSubtipo();
					System.out.println(mensaje);
					// Funcionalidad al activar el nodo de la izquierda (recibir comando)
					boolean done = false;
					if (mensaje.toString().compareTo("OP_CPU") == 0) {
						LOGGER.info("Realizando cálculo de mi CPU.");
						double sysLoad = doCPU();
						pd.getCpus().add(sysLoad);
						pd.getTokens().add(this.token);
						System.out.println(pd.getTokens() + " - " + pd.getCpus());
						done = true;
					}
					if (mensaje.toString().compareTo("OP_FILTRO") == 0) {
						LOGGER.info("Realizando operación de filtrado.");
						System.out.println("My token: " + this.token);
						if (pd.getNodoanillo().equals(this.token)) {
							String resultPath = doFiltro(pd.getFiltro(), pd.getPath());
							pd.setPath(resultPath);
							done = true;
						} else {
							LOGGER.info("Operación de filtrado no enviado a mi.");
							System.out.println("OP not sent to my token");
							done = true;
						}
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
				LOGGER.error("Class not found error while executing thread", ex);
				ex.printStackTrace();
			}
		}
	}

	public void init() {
		try {
			File file = new File(NODES);
			String last = "";
			BufferedReader br = new BufferedReader(new FileReader(file));
			last = br.readLine();
			if (file.exists()) {
				while (last != null) {
					String[] address = last.split(";");
					if (address[0].equals(Proceso1.getId())) {
						puertoIzquierda = Integer.parseInt(address[1]);
					}
					if (address[0].equals(String.valueOf(Integer.parseInt(Proceso1.getId()) + 1))) {
						puertoDerecha = Integer.parseInt(address[1]);
						System.out.println("puertoDerecha: " + puertoDerecha);
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
		Proceso1.id = id;
	}

	// --------------------------------------------------------------------------

	public double doCPU() {
		double sysLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()
				+ (new Random().nextDouble());
		System.out.println("Mi carga de CPU es de: " + sysLoad);
		return sysLoad;
	}

	public String doFiltro(String filter, String path) {
		String[] args = null;
		String resultPath = "";
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// Finds the object "hello" corba object associated with the servant previously
			// attached to the naming Service
			org.omg.CORBA.Object objRef = orb.string_to_object("corbaname::localhost:1050#Filter");

			// resolve the Object Reference in Naming
			Filter filterImpl = FilterHelper.narrow(objRef);

			System.out.println("Obtained a handle on server object: " + filterImpl);
			resultPath = filterImpl.applyFilter(filter, path);
			System.out.println("Result path: " + resultPath);

		} catch (Exception e) {
			LOGGER.error("Error while executing thread", e);
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
		return resultPath;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
