import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FilterApp.Filter;
import FilterApp.FilterHelper;
import protocol.PeticionControl;
import protocol.RespuestaControl;

public class FilterProxy {
	static Filter filterImpl;
	static ServerSocket proxy;
	private static int proxyPort = 3338;

	public FilterProxy(String[] args) {
		try {
			proxy = new ServerSocket(proxyPort);
			System.out.println("Arrancando el servidor proxy...");
			System.out.println("Abriendo canal de comunicaciones...");

			// Proxy always running
			while (true) {
				Socket sServicio = proxy.accept();
				System.out.println("Aceptada conexion de " + sServicio.getInetAddress().toString());
				ClientHandler clientSock = new ClientHandler(sServicio, args);
				new Thread(clientSock).start();
			}

		} catch (SocketException ex) {
			System.out.println("BAD CONECTION");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new FilterProxy(args);
	}

	private static class ClientHandler extends Thread {
		private final Socket clientSocket;
		private String[] args;
		private ObjectInputStream client_is, proxy_is;
		private ObjectOutputStream client_os, proxy_os;
		private PeticionControl peticionCliente;

		// Constructor
		public ClientHandler(Socket socket, String[] args) {
			this.clientSocket = socket;
			this.args = args;
		}

		public void run() {
			try {
				// start
				procesaCliente(clientSocket, args);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void procesaCliente(Socket sServicio, String[] args)
				throws UnknownHostException, IOException, ClassNotFoundException {
			try {
				// proxy actua como cliente** ante los servidores
				this.client_is = new ObjectInputStream(clientSocket.getInputStream());
				this.client_os = new ObjectOutputStream(clientSocket.getOutputStream());
				String destPath = "";

				peticionCliente = (PeticionControl) this.client_is.readObject();
				if (peticionCliente.getSubtipo().equals("OP_FILTER") && !peticionCliente.getPath().isEmpty()) {
					System.out.println("Operacion recibida!!");
					ArrayList<String> filters = peticionCliente.getArgs();
					System.out.println(filters.toString());
					// Servidor no iterativo -> El proxy realiza peticion de cada filtro individualmente
					for (int i = 0; i < filters.size(); i++) {
						try {
							// create and initialize the ORB
							ORB orb = ORB.init(args, null);

							// get the root naming context
							org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
							// Use NamingContextExt instead of NamingContext. This is
							// part of the Interoperable naming Service.
							NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

							// resolve the Object Reference in Naming
							String name = "Filter";
							filterImpl = FilterHelper.narrow(ncRef.resolve_str(name));
							System.out.println("Obtained a handle on server object: " + filterImpl);
							String filter = filters.get(i);
							System.out.println(filter);
							String path = peticionCliente.getPath();
							destPath = filterImpl.applyFilter(filter, path);

						} catch (Exception e) {
							System.out.println("ERROR : " + e);
							e.printStackTrace(System.out);
						}
						// ----------------------------------------
						System.out.println(
								"Apicado el filtro: " + filters.get(i) + " y guardado en el path: " + destPath);
					}
					RespuestaControl rc = new RespuestaControl("OK", destPath); // Path final que devuelve corba
					this.client_os.writeObject(rc);
				} else {
					System.out.println("Operación no reconocida");
					RespuestaControl rc = new RespuestaControl("NOT_OK"); // Path final que devuelve corba
					this.client_os.writeObject(rc);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (proxy_os != null)
						proxy_os.close();
					if (proxy_is != null)
						proxy_is.close();
					if (sServicio != null)
						sServicio.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
