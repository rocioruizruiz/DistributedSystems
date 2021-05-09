package multiserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Server1 {

	ServerSocket s;
	int port = 3340;
	private static int puertoRecepcion = 5000;
	private static int puertoEnvio = 5012;

	public Server1() {
		try {
			System.out.println("Arrancando el Server1...");
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
		new Server1();
	}

	// --------------------------------------------------------------------------

	public void init() {
		// Codigo de inicializacion ...
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

			} catch (Error e) {
				e.printStackTrace();
			}

		}

		// --------------------------------------------------------------------------

		public void procesaCliente(Socket sServicio) {
			try {
				System.out.println("1");

				this.is = new ObjectInputStream(sServicio.getInputStream());
				this.os = new ObjectOutputStream(sServicio.getOutputStream());

				while (true) {

					PeticionDatos pd = (PeticionDatos) this.is.readObject();

					if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) { 
						// CPUs
						ServerSocket socketIzquierda = new ServerSocket(puertoRecepcion);
						Socket socketRecepcion;

						Socket socketEnvio = new Socket("localhost", puertoEnvio);
						ObjectOutputStream outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());

						System.out.println("2");
						PeticionDatos p = new PeticionDatos("OP_CPU");
						outputEnvio.writeObject(p);

						System.out.println("3");
						String nodoprocesadoelegido = "";

						socketRecepcion = socketIzquierda.accept();
						ObjectInputStream inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
						RespuestaControl rc = (RespuestaControl) inputRecepcion.readObject();

						if (outputEnvio != null)
							outputEnvio.close();
						if (socketEnvio != null)
							socketEnvio.close();

						if (rc.getSubtipo().equals("OK")) {
							System.out.println("Cargas de CPUS recibidas!");
							System.out.println(rc.getTokens() + "\n" + rc.getCpus());
							int menor = 99999999;
							int index = 0;
							for (int i = 0; i < rc.getCpus().size(); i++) {
								if (rc.getCpus().get(i) < menor)
									index = i;
							}
							System.out.println("El de menor carga es: " + rc.getTokens().get(index));
							nodoprocesadoelegido = rc.getTokens().get(index);

							System.out.println("5");
							pd.setNodoanillo(nodoprocesadoelegido);
							System.out.println("Enviamos petición a ese nodo: " + pd.getSubtipo() + " - " + pd.getPath()
									+ " -> " + pd.getNodoanillo());

							if (inputRecepcion != null)
								inputRecepcion.close();
							if (socketRecepcion != null)
								socketRecepcion.close();

							socketEnvio = new Socket("localhost", puertoEnvio);
							outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());
							outputEnvio.writeObject(pd);

							socketRecepcion = socketIzquierda.accept();
							inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
							System.out.println("6");
							rc = (RespuestaControl) inputRecepcion.readObject();
							System.out.println(rc.getSubtipo() + " / " + rc.getPath());
							this.os.writeObject(rc);
						}
						System.out.println("4");
					}
					System.out.println("9");
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
	}
}
