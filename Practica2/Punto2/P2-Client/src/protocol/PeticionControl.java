
package protocol;

import java.util.ArrayList;

public class PeticionControl extends Peticion {

    private ArrayList args;
    private String path;

    //--------------------------------------------------------------------------

	public PeticionControl()
    {
        this.tipo = "PETICION_CONTROL";
        this.args = new ArrayList();
    }

    //--------------------------------------------------------------------------

    public PeticionControl(String subtipo)
    {
        this.tipo = "PETICION_CONTROL";
        this.subtipo = subtipo;
        this.args = new ArrayList();
    }
    
    public PeticionControl(String subtipo, String path)
    {
        this.tipo = "PETICION_CONTROL";
        this.subtipo = subtipo;
        this.args = new ArrayList();
        this.path = path;
    }

    //--------------------------------------------------------------------------

    public ArrayList getArgs() {
        return args;
    }

    //--------------------------------------------------------------------------
    
    public void setArgs(ArrayList args) {
        this.args = args;
    }  
    
    public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
