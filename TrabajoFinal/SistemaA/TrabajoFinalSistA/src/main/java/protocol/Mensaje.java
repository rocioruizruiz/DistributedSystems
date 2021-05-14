package protocol;

import java.io.Serializable;

public class Mensaje implements Serializable {

	protected String tipo;
	protected String subtipo;

	public String getSubtipo() {
		return subtipo;
	}

	public void setSubtipo(String subtipo) {
		this.subtipo = subtipo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}
