import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
	public void broadcastMessage(String message) throws RemoteException;

	public void registerClient(IClient client) throws RemoteException;
}
