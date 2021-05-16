package client;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import protocol.PeticionControl;
import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Client {

	public static final String version = "1.0";

	private Socket s;
	private ObjectOutputStream os;
	private ObjectInputStream is;
	private static int PROXY = 3338;
	private static String ip = "localhost";
	private static int portAdmin2 = 3342;
	private static String ipAdmin2 = "localhost";
	private ArrayList<Long> latencia_red = new ArrayList<Long>();
	private ArrayList<Long> latencia_app = new ArrayList<Long>();
	private long average_latencia_red = 0;
	private long average_latencia_app = 0;
	private boolean auth = false;

	private static final Logger LOGGER = LogManager.getLogger(Client.class);

	// --------------------------------------------------------------------------

	public static void main(String[] args) {
		new Client();
	}

	// --------------------------------------------------------------------------

	private void init() {

	}

	// --------------------------------------------------------------------------

	public Client() {
		this.init();

		Scanner sc = new Scanner(System.in);
		boolean finish = false;
		String credenciales[] = new String[2];

		while (!finish) {
			System.out.println("Inserte el filtro que desea aplicar o inserte 'close' para salir de la aplicación");
			String filtro = sc.nextLine();
			if (filtro.equals("close")) {
				finish = true;
				continue;
			}
			if (s == null || !auth) { // si no esta conectado
				System.out.println("Indique si tiene cuenta (y/n)");
				String reg = sc.nextLine();
				if (reg.contentEquals("y")) {
					System.out.print("Inserte su nombre de usuario:\nCliente 1.0 > ");
					credenciales[0] = sc.nextLine();
					System.out.print("Inserte su password:\nCliente 1.0 > ");
					credenciales[1] = sc.nextLine();
					doConnect(ip, PROXY);
					doFiltering(filtro, credenciales);

				} else {
					System.out.print("Inserte nombre de usuario:\nCliente 1.0 > ");
					credenciales[0] = sc.nextLine();
					System.out.print("Inserte password:\nCliente 1.0 > ");
					credenciales[1] = sc.nextLine();
					this.doConnect(ipAdmin2, portAdmin2);
					doRegister(credenciales);
					this.doDisconnect();
				}
			} else { // si ya estaba conectado
				doConnect(ip, PROXY);
				doFiltering(filtro, credenciales);
			}
		}
		if (this.s != null)
			this.doDisconnect();

		System.out.println("Saliendo de la aplicacion");
		sc.close();
	}

	// --------------------------------------------------------------------------

	private void doConnect(String ip, int port) {
		try {
			LOGGER.info("Usuario conectado correctamente.");
			// Creamos el socket
			this.s = new Socket(ip, port);
			// Asociamos los objetos al socket
			this.os = new ObjectOutputStream(s.getOutputStream());
			this.is = new ObjectInputStream(s.getInputStream());
		} catch (EOFException ex) {
			LOGGER.error("Server(proxy) error while executing thread", ex);
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!");
		} catch (ConnectException ex) {
			LOGGER.error("I/O error while executing thread", ex);
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!");
		} catch (SocketException ex) {
			LOGGER.error("Server(proxy) error while executing thread", ex);
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!");
		} catch (IOException ex) {
			LOGGER.error("I/O error while executing thread", ex);

		}
	}

	// --------------------------------------------------------------------------

	private void doDisconnect() {
		if (this.s != null) {
			try {
				// Creamos una peticion de control, la serializamos y la mandamos
				this.is.close();
				this.is = null;
				this.os.close();
				this.os = null;
				this.s.close();
				this.s = null;
			} catch (IOException ex) {
				LOGGER.error("I/O error while executing thread", ex);
			}
		} else {
			LOGGER.error("Usuario ya desconectado.");
			System.out.println("Ya estabas desconectado");
		}
	}

	// --------------------------------------------------------------------------

	private void doFiltering(String filtro, String[] credenciales) {
		try {
			// Creamos una peticion de control
			Scanner sc = new Scanner(System.in);
			PeticionDatos pd = new PeticionDatos("OP_FILTRO");
			pd.getArgs().add(credenciales[0]);
			pd.getArgs().add(credenciales[1]);

			pd.setFiltro(filtro);

			// Enviamos el objeto serializado
			long startTime = System.currentTimeMillis();
			this.os.writeObject(pd);
			long this_latency = (System.currentTimeMillis() - startTime);
			latencia_red.add((this_latency));
			System.out.println("Latencia de red actual: " + this_latency + "ms.");
			averageNetworkLatency();

			// Recibimos la respuesta de control del servidor (objeto serializado)
			RespuestaControl rc = (RespuestaControl) this.is.readObject();

			System.out.println(rc.getSubtipo());
			if (rc.getSubtipo().equals("OP_AUTH_BAD_PASSWORD")) {
				LOGGER.error("Contraseña incorrecta.");
				System.out.println("Contraseña incorrecta, inténtelo de nuevo!");
			} else if (rc.getSubtipo().equals("OP_AUTH_NO_USER")) {
				System.out.println("Este usuario no existe, desea registrarse con esas credenciales? (y/n)");
				String registrarse = sc.nextLine();
				if (registrarse.equals("y")) {
					// NODO ADMIN II
					this.doDisconnect();
					this.doConnect(ipAdmin2, portAdmin2);
					doRegister(credenciales);
					this.doDisconnect();
				} else {
					this.doDisconnect();
				}
			} else {
				auth = true;
				if (rc.getSubtipo().equals(("NOT_OK"))) {
					System.out.println("Este filtro ya no esta disponible");
				} else if (rc.getSubtipo().equals(("OK"))) {
					PeticionDatos pd2 = new PeticionDatos("OP_FILTRO");
					pd2.getArgs().add(credenciales[0]);
					pd.getArgs().add(credenciales[1]);

					pd2.setFiltro(filtro);

					LOGGER.info("Filtro disponible!");
					System.out.println("Filtro disponible! Indique la ruta de la imagen a editar: ");
					long stop = System.currentTimeMillis();
					String path = sc.nextLine();
					long restart = System.currentTimeMillis();
					long extratime = (restart-stop);
					File file = new File(path);
					if (file.exists()) {
						pd2.setPath(path);
						System.out.println(pd2.getPath());
						this.os.writeObject(pd2);
						rc = (RespuestaControl) this.is.readObject(); //
						this_latency = (System.currentTimeMillis() - startTime - extratime);
						latencia_app.add((this_latency));
						System.out.println("Tiempo de respuesta actual: " + this_latency + "ms.");
						averageAppLatency();
						System.out.println("-------------------");
						System.out.println("Resultado de la solucitud: " + rc.getSubtipo());
						LOGGER.info("Filtro correctamente aplicado");
						if (rc.getSubtipo().equals("OK"))
							System.out.println("Path final: " + rc.getPath());
					} else {
						LOGGER.warn("Ruta de imagen no existe.");
						System.out.println("Ruta de imagen no existe.");
					}
				}
			}

		} catch (ClassNotFoundException ex) {
			LOGGER.error("Class not found error while executing thread", ex);
		} catch (ConnectException ex) {
			LOGGER.error("I/O error while executing thread", ex);
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!");
		} catch (SocketException ex) {
			LOGGER.error("Server(proxy) error while executing thread", ex);
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!");
		} catch (IOException e) {
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!");
		} catch (NullPointerException ex){
			
		}
	}

	private void doRegister(String[] credenciales) {
		try {
			// Creamos una peticion de control
			PeticionControl p = new PeticionControl("OP_REGISTER");
			// Anadimos las credenciales a la lista de array
			p.getArgs().add(credenciales[0]);
			p.getArgs().add(credenciales[1]);
			// Enviamos el objeto serializado
			long startTime = System.currentTimeMillis();
			this.os.writeObject(p);
			long this_latency = (System.currentTimeMillis() - startTime);
			latencia_red.add((this_latency));
			System.out.println("Latencia de red actual: " + this_latency + "ms.");
			averageNetworkLatency();
			// Recibimos la respuesta de control del servidor (objeto serializado)
			RespuestaControl rc = (RespuestaControl) this.is.readObject();
			this_latency = (System.currentTimeMillis() - startTime);
			latencia_app.add((this_latency));
			System.out.println("Tiempo de respuesta actual: " + this_latency + "ms.");
			averageAppLatency();

			if (rc.getSubtipo().compareTo("OP_REG_OK") == 0) {
				System.out.println("Registro correcto!");
			} else if (rc.getSubtipo().compareTo("OP_REG_NOK") == 0) {
				System.out.println("El usuario ya existe, elija otro nombre");
			}
		} catch (EOFException ex) {
			LOGGER.error("Server(proxy) error while executing thread", ex);
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!");
		} catch(SocketException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			LOGGER.error("I/O error while executing thread", ex);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NullPointerException ex){
			
		}
		
	}

	// metrics
	private void averageNetworkLatency() {
		for (int i = 0; i < latencia_red.size(); i++) {
			average_latencia_red += latencia_red.get(i);
		}
		average_latencia_red = average_latencia_red / latencia_red.size();
		System.out.println("La latencia de red media es de: " + average_latencia_red + "ms.");
		average_latencia_red = 0;
	}

	private void averageAppLatency() {
		for (int i = 0; i < latencia_app.size(); i++) {
			average_latencia_app = average_latencia_app + latencia_app.get(i);
		}
		average_latencia_app = average_latencia_app / latencia_app.size();
		System.out.println("El tiempo de respuesta medio es de: " + average_latencia_app + "ms.");
		average_latencia_app = 0;
	}
}