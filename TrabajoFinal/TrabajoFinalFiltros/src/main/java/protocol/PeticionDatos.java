
package protocol;

import java.util.ArrayList;


public class PeticionDatos extends Peticion {
    private ArrayList args;
    private String filtro;

    //--------------------------------------------------------------------------

	public PeticionDatos()
    {
        this.tipo = "PETICION_DATOS";
        this.args = new ArrayList();
    }

    //--------------------------------------------------------------------------

    public PeticionDatos(String subtipo)
    {
        this.tipo = "PETICION_DATOS";
        this.subtipo = subtipo;
        this.args = new ArrayList();
        this.filtro = new String();
    }

    //--------------------------------------------------------------------------

    public ArrayList getArgs() {
        return args;
    }

    //--------------------------------------------------------------------------
    
    public void setArgs(ArrayList args) {
        this.args = args;
    } 
    
    //--------------------------------------------------------------------------

    public String getFiltro() {
		return filtro;
	}

    //--------------------------------------------------------------------------

	public void setFiltro(String filtro) {
		this.filtro = filtro;
	}

}