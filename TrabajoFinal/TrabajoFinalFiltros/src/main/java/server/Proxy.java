package server;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoDatabase;
import com.mongodb.conection.AuthDBConnection;

import protocol.Peticion;
import protocol.PeticionDatos;
import protocol.RespuestaControl;

public class Proxy {
	ServerSocket proxy;
	private static int proxyPort = 3338;
	private static int nodocentralPort = 3339;
	private static String nodocentralIP = "localhost";
	private static MongoDatabase authdb;

	public Proxy() {
		try {
			this.init();

			this.proxy = new ServerSocket(proxyPort);
			System.out.println("Arrancando el servidor proxy...");
			System.out.println("Abriendo canal de comunicaciones Proxy...");

			// Proxy Server always running
			while (true) {
				Socket sServicio = proxy.accept();
				System.out.println("Aceptada conexion de " + sServicio.getInetAddress().toString());
				ClientHandler clientSock = new ClientHandler(sServicio);
				new Thread(clientSock).start();
			}

		} catch (SocketException ex) {
			System.out.println("BAD CONECTION");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	// ---------------------------------------//

	public static void main(String[] args) {
		new Proxy();
	}

	// ---------------------------------------//

	public void init() {
		authdb = AuthDBConnection.getDB();
	}

	// ---------------------------------------//

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
			// this.init();
			try {
				// start
				procesaCliente(clientSocket);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void procesaCliente(Socket sServicio) throws UnknownHostException, IOException, ClassNotFoundException {
			try {
				// proxy actua como cliente** ante los servidores

				this.client_is = new ObjectInputStream(clientSocket.getInputStream());
				this.client_os = new ObjectOutputStream(clientSocket.getOutputStream());

				while (sServicio != null) {
					Peticion p = (Peticion) this.client_is.readObject();
					if (p.getTipo().compareTo("PETICION_DATOS") == 0) {
						PeticionDatos pd = (PeticionDatos) p;

						if (pd.getSubtipo().compareTo("OP_FILTRO") == 0) {
							this.doAuthentication(pd);
						}
					}
				}

			} catch (EOFException ex) {
				ex.printStackTrace();

			} catch (SocketException ex) {
				ex.printStackTrace();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try { // CERRAR ESTO CUANDO SE HAYA TERMINADO EL SISTEMA, AUN ESTA A MEDIAS
					System.out.println("Closing Proxy");
					if (proxy_os != null)
						proxy_os.close();
					if (proxy_is != null)
						proxy_is.close();
					if (sServicio != null)
						sServicio.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
					System.out.println("Usuario " + login + ": conectado");

					// CONECTA SERVIDOR PARA COMPROBAR SI EL FILTRO EXISTE
					this.proxySocket = new Socket(nodocentralIP, nodocentralPort);
					this.proxy_os = new ObjectOutputStream(proxySocket.getOutputStream());
					this.proxy_is = new ObjectInputStream(proxySocket.getInputStream());
					System.out.println("1");
					this.proxy_os.writeObject(pd); // el proxy le manda al nodocentral lo que dice el cliente
					System.out.println("2");
					this.client_os.writeObject(this.proxy_is.readObject()); // el proxy le manda al cliente si existe o
																			// no el filtro
					System.out.println("3");

					pd = (PeticionDatos) this.client_is.readObject();
					System.out.println("4");
					System.out.println("Path: " + pd.getPath() + " Subtipo: " + pd.getSubtipo());
					this.proxy_os.writeObject(pd); // el proxy espera la ruta de la magen y se lo envia al nodocentral
					this.client_os.writeObject(this.proxy_is.readObject()); // el proxy le manda al cliente lo que le
																			// dice el nodo central
					System.out.println("Complete");

				}

				// CONTRASEÑA INCORRECTA
				else if (exists_login != null) {
					System.out.println("Intento de acceso con contraseña incorrecta");
					RespuestaControl rc = new RespuestaControl("OP_AUTH_BAD_PASSWORD");
					client_os.writeObject(rc);

					// USUARIO NO EXISTE
				} else {
					System.out.println("Usuario no existe");
					RespuestaControl rc = new RespuestaControl("OP_AUTH_NO_USER");
					client_os.writeObject(rc);
				}
			} catch (EOFException ex) {
				System.out.println("BrokenPipe! Returning NOT_OK to client");
				try {
					this.client_os.writeObject(new RespuestaControl("NOT_OK"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
