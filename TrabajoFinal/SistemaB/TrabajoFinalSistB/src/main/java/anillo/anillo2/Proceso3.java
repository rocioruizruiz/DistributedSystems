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

public class Proceso3 {

	private int puertoIzquierda = 5014;
	private int puertoDerecha = 5015;
	private String token = "";

	private static final Logger LOGGER = LogManager.getLogger(Proceso3.class);
	private String NODES = "/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/resources/nodes.txt";
	private static String id = "15";

	public static void main(String[] args) {
		new Proceso3();
	}

	public Proceso3() {
		this.setToken(UUID.randomUUID().toString());
		this.init();
		while (true) {
			try {

				System.out.println("Arrancando el proceso 3 del anillo 2 en el puerto " + puertoIzquierda + "...");
				ServerSocket socketIzquierda = new ServerSocket(puertoIzquierda);
				Socket sIzquierda;
				while ((sIzquierda = socketIzquierda.accept()) != null) {
					// Me acaba de llegar el testigo
					LOGGER.info(
							"Proceso 3 de Anillo 2 ha aceptado la conexión " + sIzquierda.getInetAddress().toString());
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
							String filtro = getFiltroPath(pd.getFiltro());
							String resultPath = doFiltro(filtro, pd.getPath());
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
					if (address[0].equals(Proceso3.getId())) {
						puertoIzquierda = Integer.parseInt(address[1]);
					}
					if (address[0].equals("8")) {
						puertoDerecha = Integer.parseInt(address[2]);
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
		Proceso3.id = id;
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

	public String getFiltroPath(String filtro) {
		File file = new File("/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/java/anillo/anillo2");
		String thepath = "";
		if (file.exists()) {
			String[] pathnames;
			String fileName = "";
			File f = file;
			pathnames = f.list();
			for (String pathname : pathnames) {
				fileName = pathname.replaceFirst("[.][^.]+$", "");
				if (fileName.equals(filtro)) {
					thepath = f.getAbsolutePath() + "/" + pathname;
				}
			}
		}
		return thepath;
	}
}
