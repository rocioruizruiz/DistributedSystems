package protocol;

import java.util.ArrayList;

public class PeticionControl extends Peticion {

	private ArrayList args;

	// --------------------------------------------------------------------------

	public PeticionControl() {
		this.tipo = "PETICION_CONTROL";
		this.args = new ArrayList();
	}

	// --------------------------------------------------------------------------

	public PeticionControl(String subtipo) {
		this.tipo = "PETICION_CONTROL";
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
