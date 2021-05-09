package server;

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
		// Codigo de inicializacion ...
	}

	private static class ClientHandler extends Thread {

		private final Socket clientSocket;
		private ObjectInputStream is;
		private ObjectOutputStream os;
		private String PATHFILTERS = "/home/agus/eclipse-workspace/TrabajoFinalFiltros/src/main/resources/filters.txt"; // Tipos de filtros que hay  para comprobar si existen. Local del NodoCentral
		
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

				System.out.println("1");
				while (sServicio != null) {
					Peticion p = (Peticion) this.is.readObject();
					if (p.getTipo().compareTo("PETICION_DATOS") == 0) {
						System.out.println("2");
						PeticionDatos pd = (PeticionDatos) p;
						if (pd.getSubtipo().compareTo("OP_CPU") == 0)
							// this.doCPU(pc);
							System.out.println("3");

						if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) {
							this.doFiltering(pd);
							System.out.println("4");
						}
					}

				}

			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					System.out.println("Closing NodoCentral");
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
			File file = new File(PATHFILTERS);
			String last = "";
			boolean exists = false;
			try {
				if (file.exists()) {
					BufferedReader br = new BufferedReader(new FileReader(file));
					last = br.readLine();
					while (last != null) {
						if (last.equals(filtro)) {
							exists = true;
							break;
						}
						last = br.readLine();
					}
					br.close();
					if (exists) {
						System.out.println("El filtro solicitado: " + filtro + " existe!");
						RespuestaControl rc = new RespuestaControl("OK");
						this.os.writeObject(rc);

						// Aqui deberia quedarse a la escuha del path y solicitar a multiservidores.

						pd = (PeticionDatos) this.is.readObject();
						System.out.println("Path: " + pd.getPath() + " Subtipo: " + pd.getSubtipo());

						// Aqui se conectaria con el multiservidor despues de pedir las cpus y coger el
						// menor, de momento cojo server1
						// solicitoCPUSyElijomenor();

						// envioAlsolicitadoPeti();

						Socket socketEnvio = new Socket("localhost", puertoEnvio);
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
				} else {
					System.out.println("No found file");
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
