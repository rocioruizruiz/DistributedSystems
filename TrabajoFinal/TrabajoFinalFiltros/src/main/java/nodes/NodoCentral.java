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

import protocol.Peticion;
import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class NodoCentral {

	ServerSocket s;
	int port = 3339;

	private static int puertoEnvio = 3340; // envio a server1

	public NodoCentral() {
		try {
			System.out.println("Arrancando el Nodo Central...");
			this.init();
			System.out.println("Abriendo canal de comunicaciones...");
			this.s = new ServerSocket(port);
			while (true) {
				Socket sServicio = s.accept();
				System.out.println("Aceptada conexion de " + sServicio.getInetAddress().toString());
				ClientHandler clientSock = new ClientHandler(sServicio);
				new Thread(clientSock).start();
			}

		} catch (IOException e) {
			e.printStackTrace();
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

		private String PATHFILTERS = "/Volumes/pythonFilters"; // Tipos de filtros que hay para comprobar si existen. Local del NodoCentral
		private String PATHSERVERS = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/TrabajoFinalFiltros/src/main/resources/servers.txt"; // Tipos de servers que hay para conectarse. Local del NodoCentral
																									
		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				// start
				procesaCliente(clientSocket);

			} catch (Error e) {
				e.printStackTrace();
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
						System.out.println("4");
					}

				}

			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					os.close();
					is.close();
					sServicio.close();
				} catch (SocketException ex) {
					System.out.println("Broken pipe! Finishing task...");
					System.out.println("Ready to use again!");
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		// --------------------------------------------------------------------------

		public void doFiltering(PeticionDatos pd) {
			System.out.println("2");
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
				System.out.println(fileName);
				if (fileName.equals(filtro)) {
					exists = true;
					break;
				}
			}

			String last = "";
			try {
				if (exists) {
					System.out.println("El filtro solicitado: " + filtro + " existe!");
					RespuestaControl rc = new RespuestaControl("OK");
					this.os.writeObject(rc);

					pd = (PeticionDatos) this.is.readObject(); // El proxy nos manda la petición especificando la
																// ruta
					System.out.println("Path: " + pd.getPath() + " Subtipo: " + pd.getSubtipo());

					// **********************************+

					// creo petición de CPUS
					PeticionDatos pdCPU = new PeticionDatos("OP_CPU");

					// se la mando a todos los servidores

					String ipElegido = "";
					int portElegido = 3340;
					file = new File(PATHSERVERS);
					last = "";
					BufferedReader br = new BufferedReader(new FileReader(file));
					last = br.readLine();
					if (file.exists()) {
						br = new BufferedReader(new FileReader(file));
						last = br.readLine();

						double menor = 999999999.99999999;
						while (last != null) {
							String[] address = last.split(";");
							System.out.println("Creando socket: " + address[0] + " - " + address[1]);
							Socket socketCPU = new Socket(address[1], Integer.parseInt(address[0]));
							System.out.println("1");
							ObjectOutputStream os_cpu = new ObjectOutputStream(socketCPU.getOutputStream());
							System.out.println("2");
							os_cpu.writeObject(pdCPU);
							System.out.println("3");
							ObjectInputStream is_cpu = new ObjectInputStream(socketCPU.getInputStream());
							System.out.println("4");
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

					// ***********************************

					Socket socketEnvio = new Socket(ipElegido, portElegido);
					System.out.println("entra");
					ObjectOutputStream outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());

					outputEnvio.writeObject(pd);
					System.out.println("entra");

					ObjectInputStream inputEnvio = new ObjectInputStream(socketEnvio.getInputStream());

					rc = (RespuestaControl) inputEnvio.readObject();
					System.out.println(rc.getSubtipo() + " / " + rc.getPath());
					this.os.writeObject(rc);

					// this.os.writeObject(inputEnvio.readObject()); //cambiar ok por la respuesta
					// del multiservidor
					// this.os.writeObject(new RespuestaControl("OK"));
					System.out.println("OK");

				} else {
					System.out.println("El filtro solicitado: " + filtro + " NO existe!");
					RespuestaControl rc = new RespuestaControl("NOT_OK");
					this.os.writeObject(rc);
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (EOFException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}