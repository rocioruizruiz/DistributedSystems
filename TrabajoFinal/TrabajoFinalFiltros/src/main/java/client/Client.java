package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

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
			if (s == null) { // si no esta conectado
				System.out.println("Indique si tiene cuenta (y/n)");
				String reg = sc.nextLine();
				if (reg.contentEquals("y")) {
					System.out.print("Inserte su nombre de usuario:\nCliente 1.0 >");
					credenciales[0] = sc.nextLine();
					System.out.print("Inserte su password:\nCliente 1.0 >");
					credenciales[1] = sc.nextLine();
					doConnect(ip, PROXY);
					doFiltering(filtro, credenciales);

				} else {
					System.out.print("Inserte nombre de usuario:\nCliente 1.0 >");
					credenciales[0] = sc.nextLine();
					System.out.print("Inserte password:\nCliente 1.0 >");
					credenciales[1] = sc.nextLine();
					this.doConnect(ipAdmin2, portAdmin2);
					doRegister(credenciales);
					this.doDisconnect();
				}
			} else { // si ya estaba conectado
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
			// if(this.s == null){
			// Creamos el socket
			this.s = new Socket(ip, port);
			// Asociamos los objetos al socket
			this.os = new ObjectOutputStream(s.getOutputStream());
			this.is = new ObjectInputStream(s.getInputStream());
			// }
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (EOFException ex) {
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!");
		} catch (IOException ex) {
			ex.printStackTrace();
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
				ex.printStackTrace();
			}
		} else {
			System.out.println("Ya estabas desconectado");
		}
	}

	// --------------------------------------------------------------------------
	// CREAR: doFiltering
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
			if (rc.getSubtipo().equals("OP_AUTH_BAD_PASSWORD"))
				System.out.println("Contraseña incorrecta, inténtelo de nuevo!");

			else if (rc.getSubtipo().equals("OP_AUTH_NO_USER")) {

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

				if (rc.getSubtipo().equals(("NOT_OK"))) {
					System.out.println("Este filtro ya no esta disponible");
					s.close();
				} else if (rc.getSubtipo().equals(("OK"))) {

					PeticionDatos pd2 = new PeticionDatos("OP_FILTRO");
					pd2.getArgs().add(credenciales[0]);
					pd.getArgs().add(credenciales[1]);

					pd2.setFiltro(filtro);

					System.out.println("Filtro disponible! Indique la ruta de la imagen a editar: ");
					String path = sc.nextLine();
					pd2.setPath(path);
					System.out.println(pd2.getPath());
					this.os.writeObject(pd2);
					rc = (RespuestaControl) this.is.readObject(); //
					this_latency = (System.currentTimeMillis() - startTime);
					latencia_app.add((this_latency));
					System.out.println("Tiempo de respuesta actual: " + this_latency + "ms.");
					averageAppLatency();
					System.out.println("-------------------");
					System.out.println("OK");
				}
			}

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (EOFException ex) {
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!");
		} catch (IOException ex) {
			ex.printStackTrace();
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
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (EOFException ex) {
			System.out.println("Se ha producido un error en el servidor(proxy), intentelo de nuevo!!!");
		} catch (IOException ex) {
			ex.printStackTrace();
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