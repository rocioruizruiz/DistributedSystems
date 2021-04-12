package client;

import java.io.*;

public class Consola {

    public static String prompt = "Cliente v " + Client.version + "> ";

    private InputStreamReader isr;
    private BufferedReader br;

    //--------------------------------------------------------------------------

    public Consola()
    {
        this.isr = new InputStreamReader(System.in);
        this.br = new BufferedReader(isr);
    }

    //--------------------------------------------------------------------------

    public void writeMessage(String msg)
    {
        System.out.println( "> " + msg );
    }

    //--------------------------------------------------------------------------
    
    public String getCommand()
    {
        String line = null;
        try {
            System.out.print(Consola.prompt);
            line = this.br.readLine();
           
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        return line;
    }

    //--------------------------------------------------------------------------

    public String getCommandGET()
    {
        String line = null;
        try {
            System.out.print("Indique el nombre del fichero que desea descargar ");
            line = this.br.readLine();

        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        return line;
    }

    //--------------------------------------------------------------------------

    public String getCommandPUT()
    {
        String line = null;
        try {
            System.out.print("Indique la ruta al fichero que desea ");
            line = this.br.readLine();

        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        return line;
    }

    //--------------------------------------------------------------------------

    public String getCommandDELETE()
    {
        String line = null;
        try {
            System.out.print("Indique el nombre del fichero que desea borrar ");
            line = this.br.readLine();

        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        return line;
    }

    //--------------------------------------------------------------------------


    public String getCommandMENSAJE() 
    {
    	String mensaje = new String();
        try {
            System.out.print("INTRODUZCA MENSAJE A DESENCRIPTAR: ");
            mensaje = this.br.readLine();
            
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        return mensaje;
    
    }
}