package server;

public class Usuario {
	public static final int USER_BAD_PASSWORD = 401; 
	public static final int USER_NO_LOGIN = 400;
	public static final int USER_OK = 200;
	private String login;
	private String password;
	
	public Usuario() {};
	public Usuario(String login_, String password_) {
		login = login_;
		password = password_;
	};
	
	
	
	// ------- GETTER & SETTER 's ----------------
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
}
