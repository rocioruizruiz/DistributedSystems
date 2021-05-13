package nodes;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import protocol.Peticion;
import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class NodoCentral {

	private static final Logger LOGGER = LogManager.getLogger(NodoCentral.class);

	ServerSocket s;
	int port = 3339;

	public NodoCentral() {
		try {
			System.out.println("Arrancando el Nodo Central...");
			this.init();
			System.out.println("Abriendo canal de comunicaciones...");
			this.s = new ServerSocket(port);
			while (true) {
				Socket sServicio = s.accept();
				LOGGER.info("Nodo central ha aceptado la conexión " + sServicio.getInetAddress().toString());
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
		new NodoCentral();
	}

	// --------------------------------------------------------------------------

	public void init() {

	}

	private static class ClientHandler extends Thread {

		private final Socket clientSocket;
		private ObjectInputStream is;
		private ObjectOutputStream os;

		private String PATHFILTERS = "/mnt/clientPythonFilter/"; // Tipos de filtros que hay para comprobar si existen. Local del NodoCentral
		private String PATHSERVERS = "/home/agus/eclipse-workspace/TrabajoFinalFiltros/src/main/resources/servers.txt"; // Puertos de servers que hay para conectarse. Local del NodoCentral
		
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

				Peticion p = (Peticion) this.is.readObject();
				if (p.getTipo().compareTo("PETICION_DATOS") == 0) {
					PeticionDatos pd = (PeticionDatos) p;
					if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) {
						this.doFiltering(pd);
					}
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
					LOGGER.error("Broken pipe!", ex);
					System.out.println("Broken pipe! Finishing task...");
					System.out.println("Ready to use again!");
				} catch (IOException ex) {
					LOGGER.error("I/O error while executing thread", ex);
					ex.printStackTrace();
				}
			}
		}

		// --------------------------------------------------------------------------

		public void doFiltering(PeticionDatos pd) {
			String filtro = pd.getFiltro();
			System.out.println(filtro);
			File file = new File(PATHFILTERS);
			String[] pathnames;
			String fileName = "";
			File f = file;
			pathnames = f.list();
			boolean exists = false;
			for (String pathname : pathnames) {
				fileName = pathname.replaceFirst("[.][^.]+$", "");
				if (fileName.equals(filtro)) {
					exists = true;
					break;
				}
			}

			String last = "";
			Socket socketEnvio = null;
			try {
				if (exists) {
					LOGGER.info("El filtro solicitado existe.");
					System.out.println("El filtro solicitado: " + filtro + " existe!");
					RespuestaControl rc = new RespuestaControl("OK");
					this.os.writeObject(rc);
					pd = (PeticionDatos) this.is.readObject(); // El proxy nos manda la petición especificando la ruta
					System.out.println("Path: " + pd.getPath() + " Subtipo: " + pd.getSubtipo());

					// creo petición de CPUS y SYNCHRONIZATION
					PeticionDatos pdCPU = new PeticionDatos("OP_CPU");
					PeticionDatos pdSync = new PeticionDatos("OP_SYNC");

					// se la mando a todos los servidores
					String ipElegido = "";
					int portElegido = 3340;
					file = new File(PATHSERVERS);
					last = "";
					BufferedReader br = new BufferedReader(new FileReader(file));
					last = br.readLine();
					
					if (file.exists()) {
						last = br.readLine();
						double menor = 999999999.99999999;
						
						while (last != null) {
							String[] address = last.split(";");
							
							// SYNCHRONIZATION
							Socket socketSync = new Socket(address[1], Integer.parseInt(address[0]));
							ObjectOutputStream os_sync = new ObjectOutputStream(socketSync.getOutputStream());
							os_sync.writeObject(pdSync);
							ObjectInputStream is_sync = new ObjectInputStream(socketSync.getInputStream());
							Thread.sleep(1000);
							long t = System.currentTimeMillis();
							pd.getTime().add(t);
							// recibo respuestas
							RespuestaControl rc_sync = (RespuestaControl) is_sync.readObject();
							System.out.println("Tiempos: " + rc_sync.getTime());
							Long suma = (long) 0;
							for (int i = 0; i < rc_sync.getTime().size(); i++) {
								suma = rc_sync.getTime().get(i);
								suma += suma;
							}
							long mediaTiempos = suma / rc_sync.getTime().size();
							long correction = (t - mediaTiempos) / 2;
							Date relojHoras = new Date(correction);
							DateFormat hourFormat = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
							LOGGER.info("El reloj ha sido sincronizado.");
							System.out.println(
									"El reloj se ha sincronizado y la hora es " + hourFormat.format(relojHoras));

							// CPU
							System.out.println("Creando socket: " + address[0] + " - " + address[1]);
							Socket socketCPU = new Socket(address[1], Integer.parseInt(address[0]));
							ObjectOutputStream os_cpu = new ObjectOutputStream(socketCPU.getOutputStream());
							os_cpu.writeObject(pdCPU);
							ObjectInputStream is_cpu = new ObjectInputStream(socketCPU.getInputStream());
							// recibo respuestas
							RespuestaControl rc_CPUS = (RespuestaControl) is_cpu.readObject();
							System.out.println("CPUS: " + rc_CPUS.getCpus().get(0));
							if (rc_CPUS.getCpus().size() > 0 && rc_CPUS.getCpus().get(0) < menor) {
								// elijo el menor
								ipElegido = address[1];
								portElegido = Integer.parseInt(address[0]);
								menor = rc_CPUS.getCpus().get(0);
							}
							last = br.readLine();
						}
						br.close();
					}
					socketEnvio = new Socket(ipElegido, portElegido);
					ObjectOutputStream outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());
					outputEnvio.writeObject(pd);
					ObjectInputStream inputEnvio = new ObjectInputStream(socketEnvio.getInputStream());
					rc = (RespuestaControl) inputEnvio.readObject();
					System.out.println("Resultado de operación: " + rc.getSubtipo() + " - Ruta final: " + rc.getPath());
					this.os.writeObject(rc);
				} else {
					LOGGER.error("El filtro solicitado no existe.");
					System.out.println("El filtro solicitado: " + filtro + " NO existe!");
					RespuestaControl rc = new RespuestaControl("NOT_OK");
					this.os.writeObject(rc);
				}
			} catch (FileNotFoundException ex) {
				LOGGER.error("File not found error while executing thread", ex);
				ex.printStackTrace();
			} catch (EOFException ex) {
				LOGGER.error("? error while executing thread", ex);
				ex.printStackTrace();
			} catch (SocketException ex) {
				LOGGER.error("Socket error while executing thread", ex);
				ex.printStackTrace();
			} catch (IOException ex) {
				LOGGER.error("I/O error while executing thread", ex);
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				LOGGER.error("Class not found error while executing thread", ex);
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				LOGGER.error("Interrupted sleep thread error while executing thread", ex);
				ex.printStackTrace();
			} finally {
				if (socketEnvio != null)
					try {
						socketEnvio.close();
						if (this.is != null)
							this.is.close();
						if (this.os != null)
							this.os.close();
					} catch (IOException ex) {
						LOGGER.error("I/O error while executing thread", ex);
						ex.printStackTrace();
					}
			}
		}
	}
}