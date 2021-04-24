import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Server extends UnicastRemoteObject implements IServer {

	private static final long serialVersionUID = 1L;
	public List<IClient> clients;
	public String name;

	public Server() throws RemoteException {
		super();
		this.clients = new ArrayList<>();
	}

	public void broadcastMessage(String message) throws RemoteException {
		for (IClient client : clients) {
			client.retrieveMessage(message);
		}
		System.out.println(message);
	}

	public void registerClient(IClient client) throws RemoteException {
		this.clients.add(client);
	}

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		String name = "Server";
		try {
			Server chatServer = new Server();
			Naming.rebind(name, chatServer);
			System.out.println("Server bound");
		} catch (RemoteException e) {
			System.err.println("Error: RMI Registry not initialized. Exception info: \n" + e.toString());
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Server error. Exception info: " + e.getMessage());
			System.exit(1);
		}
	}
}
