package admins;

import static com.mongodb.client.model.Filters.eq;

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

import protocol.PeticionControl;
import protocol.RespuestaControl;

public class Admin2 {

	ServerSocket admin2;
	private static int admin2Port = 3342;
	private static MongoDatabase authdb;

	public Admin2() {
		try {
			this.init();

			this.admin2 = new ServerSocket(admin2Port);
			System.out.println("Arrancando el admin2 ...");
			System.out.println("Abriendo canal de comunicaciones admin2 ...");

			// Proxy Server always running
			while (true) {
				Socket sServicio = admin2.accept();
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

	public static void main(String[] args) {
		new Admin2();
	}

	public void init() {
		authdb = AuthDBConnection.getDB();
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

				this.is = new ObjectInputStream(clientSocket.getInputStream());
				this.os = new ObjectOutputStream(clientSocket.getOutputStream());
				PeticionControl pc = (PeticionControl) this.is.readObject();

				if (pc.getSubtipo().compareTo("OP_REGISTER") == 0) {
					this.doRegister(pc);
				} else {
					os.writeObject(new RespuestaControl("NOT_OK"));
				}

			} catch (ClassNotFoundException ex) {
			} catch (IOException ex) {
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
		// CREAR: Metodo para registro de usuarios
		public void doRegister(PeticionControl pc) {

			String username = (String) pc.getArgs().get(0);
			String password = (String) pc.getArgs().get(1);
			try {
				Bson filter_username = (eq("username", username));
				Document exists_username = authdb.getCollection("Users").find(filter_username).first();

				if (exists_username != null) {
					RespuestaControl rc = new RespuestaControl("OP_REG_NOK");
					this.os.writeObject(rc);
					System.out.println("Usuario " + username + " no registrado porque ya existe");

				} else {
					authdb.getCollection("Users")
							.insertOne(new Document("username", username).append("password", password));
					RespuestaControl rc = new RespuestaControl("OP_REG_OK");
					this.os.writeObject(rc);
					System.out.println("Usuario registrado satisfactoriamente");
				}
			} catch (SocketException ex) {
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}
}