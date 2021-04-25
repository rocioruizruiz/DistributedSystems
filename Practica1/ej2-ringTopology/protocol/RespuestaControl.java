package protocol;

import java.util.ArrayList;

public class RespuestaControl extends Respuesta {
	private ArrayList args;

	// --------------------------------------------------------------------------

	public RespuestaControl() {
		this.tipo = "RESPUESTA_CONTROL";
		this.args = new ArrayList();
	}

	// --------------------------------------------------------------------------

	public RespuestaControl(String subtipo) {
		this.tipo = "RESPUESTA_CONTROL";
		this.subtipo = subtipo;
		this.args = new ArrayList();
	}

	// --------------------------------------------------------------------------

	public ArrayList getArgs() {
		return args;
	}

	// --------------------------------------------------------------------------

	public void setArgs(ArrayList args) {
		this.args = args;
	}
}
