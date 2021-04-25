
package protocol;


import java.util.ArrayList;


public class RespuestaControl extends Respuesta {
    private ArrayList args;
    private String path;

    //--------------------------------------------------------------------------

    public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public RespuestaControl()
    {
        this.tipo = "RESPUESTA_CONTROL";
        this.args = new ArrayList();
    }

    //--------------------------------------------------------------------------

	public RespuestaControl(String subtipo)
    {
        this.tipo = "RESPUESTA_CONTROL";
        this.subtipo = subtipo;
        this.args = new ArrayList();
    }
	
    public RespuestaControl(String subtipo, String path)
    {
        this.tipo = "RESPUESTA_CONTROL";
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
}
