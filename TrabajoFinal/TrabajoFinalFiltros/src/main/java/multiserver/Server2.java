package multiserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Server2 {

	ServerSocket s;
	int port = 3341;
	private static int puertoRecepcionNodoControl = 5010;
	private static int puertoEnvioNodoControl = 5011;

	public Server2() {
		try {
			System.out.println("Arrancando el Server2 ...");
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

	public static void main(String[] args) throws IOException {
		new Server2();
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

				this.is = new ObjectInputStream(sServicio.getInputStream());
				this.os = new ObjectOutputStream(sServicio.getOutputStream());


				PeticionDatos pd = (PeticionDatos) this.is.readObject();

				if (pd.getSubtipo().compareTo("OP_CPU") == 0) {
					double sysLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()
							+ (new Random().nextDouble());
					System.out.println("Mi carga de CPU es de: " + sysLoad);
					RespuestaControl rc_cpus = new RespuestaControl("OK");
					rc_cpus.getCpus().add(sysLoad);
					this.os.writeObject(rc_cpus);
				}

				if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) {
					// CPUs
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
						System.out.println("Cargas de CPUS recibidas!");
						System.out.println(rc.getTokens() + "\n" + rc.getCpus());
						double menor = 99999999.99999;
						int index = 0;
						for (int i = 0; i < rc.getCpus().size(); i++) {
							if (rc.getCpus().get(i) < menor)
								System.out.println(rc.getCpus().get(i) + " es menor que: " + menor);
								index = i;
								menor = rc.getCpus().get(i);
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

						socketEnvio = new Socket("localhost", puertoEnvioNodoControl);
						outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());
						outputEnvio.writeObject(pd);

						socketRecepcion = socketIzquierda.accept();
						inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
						rc = (RespuestaControl) inputRecepcion.readObject();
						System.out.println("Resultado de la operación: " + rc.getSubtipo() + " - Ruta final: " + rc.getPath());
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
	}
}