import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements IClient {

	private static final long serialVersionUID = 1L;
	private String name;

	public Client(String name, IServer chatServer) throws RemoteException {
		this.name = name;
		chatServer.registerClient(this);
		String userStatus = "User [" + this.name + "] connected.";
		chatServer.broadcastMessage(userStatus);
	}

	public String getName() throws RemoteException {
		return this.name;
	}

	public void retrieveMessage(String message) throws RemoteException {
		System.out.println(message);
	}

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		try {
			Scanner sc = new Scanner(System.in);
			System.out.print("User name: ");
			String user = sc.nextLine();

			String name = "rmi://" + args[0] + "/Server";
			IServer chatServer = (IServer) Naming.lookup(name);
			Client chatClient = new Client(user, chatServer);

			while (true) {
				String message = sc.nextLine();
				message = "[" + chatClient.getName() + "] " + message;
				chatServer.broadcastMessage(message);
			}
		} catch (RemoteException e) {
			System.err.println("Error: Unreachable host. Exception info: \n" + e.toString());
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Client error. Exception info: \n" + e.getMessage());
			System.exit(1);
		}
	}
}
