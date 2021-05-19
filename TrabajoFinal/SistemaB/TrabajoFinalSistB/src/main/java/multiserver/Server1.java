package multiserver;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Server1 {

	ServerSocket s;
	int port = 3340;
	private static int puertoRecepcionNodoControl;
	private static int puertoEnvioNodoControl;

	private static final Logger LOGGER = LogManager.getLogger(Server1.class);
	private String NODES = "/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/resources/nodes.txt";
	private static String id = "4";
	private static String FILTERS = "/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/resources/filters.txt";

	public Server1() {
		try {
			this.init();
			System.out.println("Arrancando el Server 1 en el puerto " + port + "...");
			System.out.println("Abriendo canal de comunicaciones...");
			this.s = new ServerSocket(port);
			while (true) {
				Socket sServicio = s.accept();
				LOGGER.info("Server 1 ha aceptado la conexión " + sServicio.getInetAddress().toString());
				System.out.println("Aceptada conexion de " + sServicio.getInetAddress().toString());
				ClientHandler clientSock = new ClientHandler(sServicio);
				new Thread(clientSock).start();
			}
		} catch (IOException ex) {
			LOGGER.error("I/O error while executing thread", ex);
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server1();
	}

	// --------------------------------------------------------------------------

	public void init() {
		try {
			File file = new File(NODES);
			String last = "";
			BufferedReader br = new BufferedReader(new FileReader(file));
			last = br.readLine();
			if (file.exists()) {
				while (last != null) {
					String[] address = last.split(";");
					if (address[0].equals(Server1.getId())) {
						port = Integer.parseInt(address[1]);
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
		Server1.id = id;
	}

	private static class ClientHandler extends Thread {
		private final Socket clientSocket;
		private ObjectInputStream is;
		private ObjectOutputStream os;

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				// start
				procesaCliente(clientSocket);
			} catch (Error ex) {
				LOGGER.error("Error while executing thread", ex);
				ex.printStackTrace();
			}
		}

		// --------------------------------------------------------------------------

		public void procesaCliente(Socket sServicio) {
			try {
				this.is = new ObjectInputStream(sServicio.getInputStream());
				this.os = new ObjectOutputStream(sServicio.getOutputStream());

				PeticionDatos pd = (PeticionDatos) this.is.readObject();

				if (pd.getSubtipo().compareTo("OP_SYNC") == 0) {
					LOGGER.info("Realizando operación de sincronización.");
					long t1 = System.currentTimeMillis();
					System.out.println("Mi tiempo actual es de: " + t1);
					RespuestaControl rc_sync = new RespuestaControl("OK");
					rc_sync.getTime().add(t1);
					this.os.writeObject(rc_sync);
				}

				if (pd.getSubtipo().compareTo("OP_CPU") == 0) {
					LOGGER.info("Realizando cálculo de mi CPU.");
					double sysLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()
							+ (new Random().nextDouble());
					System.out.println("Mi carga de CPU es de: " + sysLoad);
					RespuestaControl rc_cpus = new RespuestaControl("OK");
					rc_cpus.getCpus().add(sysLoad);
					this.os.writeObject(rc_cpus);
				}

				if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) {
					File file = new File(FILTERS);
					String last = "";
					BufferedReader br = new BufferedReader(new FileReader(file));
					last = br.readLine();
					if (file.exists()) {
						while (last != null) {
							String[] address = last.split(";");
							if (address[0].equals(pd.getFiltro())) {
								int recepcion = Integer.parseInt(address[1]);
								puertoRecepcionNodoControl = recepcion;
								int envio = Integer.parseInt(address[2]);
								puertoEnvioNodoControl = envio;
								break;
							}
							last = br.readLine();
						}
					}
					br.close();

					ServerSocket socketIzquierda = new ServerSocket(puertoRecepcionNodoControl);
					Socket socketRecepcion;
					Socket socketEnvio = new Socket("localhost", puertoEnvioNodoControl);
					ObjectOutputStream outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());

					PeticionDatos p = new PeticionDatos("OP_CPU");
					outputEnvio.writeObject(p);
					String nodoprocesadoelegido = "";

					socketRecepcion = socketIzquierda.accept();
					ObjectInputStream inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
					RespuestaControl rc = (RespuestaControl) inputRecepcion.readObject();

					if (outputEnvio != null)
						outputEnvio.close();
					if (socketEnvio != null)
						socketEnvio.close();

					if (rc.getSubtipo().equals("OK")) {
						try {
							System.out.println("Cargas de CPUS recibidas!");
							System.out.println(rc.getTokens() + "\n" + rc.getCpus());
							double menor = 99999999.99999;
							int index = 0;
							for (int i = 0; i < rc.getCpus().size(); i++) {
								if (rc.getCpus().get(i) < menor) {
									System.out.println(rc.getCpus().get(i) + " es menor que: " + menor);
									index = i;
									menor = rc.getCpus().get(i);
								}
							}
							System.out.println("El de menor carga es: " + rc.getTokens().get(index));
							nodoprocesadoelegido = rc.getTokens().get(index);
							pd.setNodoanillo(nodoprocesadoelegido);
							LOGGER.info("Enviando petición a nodo: " + pd.getSubtipo() + " - " + pd.getPath() + " -> "
									+ pd.getNodoanillo());
							System.out.println("Enviamos petición a ese nodo: " + pd.getSubtipo() + " - " + pd.getPath()
									+ " -> " + pd.getNodoanillo());

							if (inputRecepcion != null)
								inputRecepcion.close();
							if (socketRecepcion != null)
								socketRecepcion.close();

							socketEnvio = new Socket("localhost", puertoEnvioNodoControl);
							outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());
							outputEnvio.writeObject(pd);

							socketRecepcion = socketIzquierda.accept();
							inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
							rc = (RespuestaControl) inputRecepcion.readObject();
							System.out.println(
									"Resultado de la operación: " + rc.getSubtipo() + " - Ruta final: " + rc.getPath());
							this.os.writeObject(rc);

							if (inputRecepcion != null)
								inputRecepcion.close();
							if (socketRecepcion != null)
								socketRecepcion.close();
							if (outputEnvio != null)
								outputEnvio.close();
							if (socketEnvio != null)
								socketEnvio.close();
							if (socketIzquierda != null) {
								socketIzquierda.close();
							}
						} catch (ConnectException ex) {
							System.out.println("Se ha producido un error de conexion");
							if (inputRecepcion != null)
								inputRecepcion.close();
							if (socketRecepcion != null)
								socketRecepcion.close();
							if (outputEnvio != null)
								outputEnvio.close();
							if (socketEnvio != null)
								socketEnvio.close();
							if (socketIzquierda != null) {
								socketIzquierda.close();
							}
						} catch (EOFException ex) {
							System.out.println("Se ha producido un error de conexion");
							if (inputRecepcion != null)
								inputRecepcion.close();
							if (socketRecepcion != null)
								socketRecepcion.close();
							if (outputEnvio != null)
								outputEnvio.close();
							if (socketEnvio != null)
								socketEnvio.close();
							if (socketIzquierda != null) {
								socketIzquierda.close();
							}
						}
					}
				}
			} catch (ConnectException ex) {
				LOGGER.error("Conection  error while executing thread", ex);
				System.out.println("Se ha producido un error de conexión");
				try {
					if (clientSocket != null)
						clientSocket.close();
					if (sServicio != null)
						sServicio.close();
					if (this.is != null)
						this.is.close();
					if (this.os != null)
						this.os.close();
				} catch (IOException e) {
					LOGGER.error("I/O error while executing thread", ex);
					e.printStackTrace();
				}
			} catch (ClassNotFoundException ex) {
				LOGGER.error("Class not found error while executing thread", ex);
				ex.printStackTrace();
			} catch (IOException ex) {
				LOGGER.error("I/O error while executing thread", ex);
				ex.printStackTrace();
			} finally {
				try {
					os.close();
					is.close();
					sServicio.close();
				} catch (SocketException ex) {
					LOGGER.error("Socket error while executing thread", ex);
					System.out.println("Broken pipe! Finishing task...");
					System.out.println("Ready to use again!");
				} catch (IOException ex) {
					LOGGER.error("I/O error while executing thread", ex);
					ex.printStackTrace();
				}
			}
		}
	}
}