package nodes;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

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
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoDatabase;
import com.mongodb.conection.AuthDBConnection;

import protocol.Peticion;
import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Proxy {
	ServerSocket proxy;
	private static String id = "2";
	private static int proxyPort = 3338;
	private static int nodocentralPort = 3339;
	private static String nodocentralIP = "localhost";
	private static MongoDatabase authdb;
	private String NODES = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/TrabajoFinalFiltros/src/main/resources/nodes.txt";

	private static final Logger LOGGER = LogManager.getLogger(Proxy.class);

	public Proxy() {
		try {
			this.init();

			this.proxy = new ServerSocket(proxyPort);
			System.out.println("Arrancando el servidor proxy en el puerto " + proxyPort + "...");
			System.out.println("Abriendo canal de comunicaciones Proxy...");

			// Proxy Server always running
			while (true) {
				Socket sServicio = proxy.accept();
				LOGGER.info("Proxy ha aceptado la conexión " + sServicio.getInetAddress().toString());
				System.out.println("Aceptada conexion de " + sServicio.getInetAddress().toString());
				ClientHandler clientSock = new ClientHandler(sServicio);
				new Thread(clientSock).start();
			}

		} catch (SocketException ex) {
			LOGGER.error("BAD CONECTION: ", ex);
			System.out.println("BAD CONECTION");

		} catch (IOException ex) {
			LOGGER.error("I/O error while executing thread", ex);
			ex.printStackTrace();
		}
	}
	
	// ---------------------------------------//

	public static void main(String[] args) {
		new Proxy();
	}

	// ---------------------------------------//

	public void init() {
		try {
			authdb = AuthDBConnection.getDB();
			File file = new File(NODES);
			String last = "";
			BufferedReader br = new BufferedReader(new FileReader(file));
			last = br.readLine();
			if (file.exists()) {
				while (last != null) {
					String[] address = last.split(";");
					if(address[0].equals(Proxy.getId())) {
						proxyPort = Integer.parseInt(address[1]);
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

	// ---------------------------------------//

	public static String getId() {
		return id;
	}

	public static void setId(String id) {
		Proxy.id = id;
	}

	private static class ClientHandler extends Thread {

		private final Socket clientSocket;
		private Socket proxySocket;
		private ObjectInputStream client_is, proxy_is;
		private ObjectOutputStream client_os, proxy_os;

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				// start
				procesaCliente(clientSocket);
			} catch (ClassNotFoundException ex) {
				LOGGER.error("Class not found error while executing thread", ex);
				ex.printStackTrace();
			} catch (IOException ex) {
				LOGGER.error("I/O error while executing thread", ex);
				ex.printStackTrace();
			}
		}

		public void procesaCliente(Socket sServicio) throws UnknownHostException, IOException, ClassNotFoundException {
			try {
				
				this.client_is = new ObjectInputStream(clientSocket.getInputStream());
				this.client_os = new ObjectOutputStream(clientSocket.getOutputStream());

				Peticion p = (Peticion) this.client_is.readObject();
				if (p.getTipo().compareTo("PETICION_DATOS") == 0) {
					PeticionDatos pd = (PeticionDatos) p;
					if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) {
						this.doAuthentication(pd);
					}
				}


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
			} finally {
				if (proxy_os != null)
					proxy_os.close();
				if (proxy_is != null)
					proxy_is.close();
				if (sServicio != null)
					sServicio.close();
			}
		}

		public void doAuthentication(PeticionDatos pd) {

			String login = (String) pd.getArgs().get(0);
			String password = (String) pd.getArgs().get(1);
			try {
				Bson filter_login = (eq("username", login));
				Bson filter = and((eq("username", login)), (eq("password", password)));
				Document exists = authdb.getCollection("Users").find(filter).first();
				Document exists_login = authdb.getCollection("Users").find(filter_login).first();

				// AUTENTIFICACION CORRECTA
				if (exists != null) {
					LOGGER.info("Autentificación de usuario correcta.");
					System.out.println("Usuario " + login + ": conectado");

					// CONECTA SERVIDOR PARA COMPROBAR SI EL FILTRO EXISTE
					this.proxySocket = new Socket(nodocentralIP, nodocentralPort);
					this.proxy_os = new ObjectOutputStream(proxySocket.getOutputStream());
					this.proxy_is = new ObjectInputStream(proxySocket.getInputStream());
					this.proxy_os.writeObject(pd); // el proxy le manda al nodocentral lo que dice el cliente
					RespuestaControl rc = (RespuestaControl) this.proxy_is.readObject();
					this.client_os.writeObject(rc); // el proxy le manda al cliente si existe o no el filtro
					if (rc.getSubtipo().contentEquals("OK")) {
						pd = (PeticionDatos) this.client_is.readObject();
						System.out.println("Path: " + pd.getPath() + " Subtipo: " + pd.getSubtipo());
						this.proxy_os.writeObject(pd); // el proxy espera la ruta de la magen y se lo envia al nodocentral
						this.client_os.writeObject(this.proxy_is.readObject()); // el proxy le manda al cliente lo que le dice el nodo central
						System.out.println("Complete");
					}
				}

				// CONTRASEÑA INCORRECTA
				else if (exists_login != null) {
					LOGGER.warn("Contraseña introducida incorrecta.");
					System.out.println("Intento de acceso con contraseña incorrecta");
					RespuestaControl rc = new RespuestaControl("OP_AUTH_BAD_PASSWORD");
					client_os.writeObject(rc);

					// USUARIO NO EXISTE
				} else {
					LOGGER.warn("El usuario no existe.");
					System.out.println("Usuario no existe");
					RespuestaControl rc = new RespuestaControl("OP_AUTH_NO_USER");
					client_os.writeObject(rc);
				}
			} catch (EOFException ex) {
				LOGGER.error("Broken Pipe!", ex);
				System.out.println("BrokenPipe! Returning NOT_OK to client");
				try {
					if(this.client_os != null) this.client_os.writeObject(new RespuestaControl("NOT_OK"));
				} catch (IOException e) {
					LOGGER.error("I/O error while executing thread", ex);
					e.printStackTrace();
				}
			} catch (IOException ex) {
				LOGGER.error("I/O error while executing thread", ex);
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				LOGGER.error("Class not found error while executing thread", ex);
				ex.printStackTrace();
			}finally {
				
				try {
					if(this.client_is != null) this.client_is.close();
					if(this.client_os != null) this.client_os.close();
					if(this.proxy_is != null) this.proxy_is.close();
					if(this.proxy_os != null) this.proxy_os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}