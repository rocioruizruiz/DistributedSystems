package server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileUsers {
	
	private ArrayList<Usuario> users = new ArrayList<Usuario>();
	
	public FileUsers() {
		try {
			getUsers(new File("/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/ComunicacionProcesos/src/server/users.txt"));//ComunicacionProcesos/src/server/users.txt
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	};
	
	public int findUser(String login, String password, Usuario userData) {
		for(Usuario u : users) {
			if(u.getLogin().equals(login)) {		
				if(u.getPassword().equals(password)) {
					userData.setLogin(login);
					userData.setPassword(password);
					return 200;
				}else {
					return 401;
				}
			}
		}
		return 400; // No hay users
	}

	public int findUser(String login) {
		for(Usuario u : users) {
			if(u.getLogin().equals(login)) {
				return 200;
			}
		}
		return 400;
	}

	public void writeUser(String login, String password) {
		users.add(new Usuario(login, password));		
	}
	
    public void getUsers(File archivo) throws FileNotFoundException, IOException {
        String cadena;
        String[] userData; //userData[0] = login and userData[1] = password;
        FileReader f = new FileReader(archivo);
        BufferedReader b = new BufferedReader(f);
        while((cadena = b.readLine())!=null) {
            userData = cadena.split(",");
            this.writeUser(userData[0], userData[1]);
        }
        b.close();
    }
}



    

   

