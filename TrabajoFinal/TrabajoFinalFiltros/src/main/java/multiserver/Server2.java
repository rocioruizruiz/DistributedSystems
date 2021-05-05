package multiserver;

import java.io.*;
import java.net.*;
import java.util.Scanner;


import protocol.*;

public class Server2 {

    ServerSocket s;
    int port = 3339;
    private static int puertoRecepcion = 5000;
	private static int puertoEnvio = 5012;
   
    public Server2() {
    	try {
    		System.out.println("Arrancando el Nodo Central...");
            this.init();
            System.out.println("Abriendo canal de comunicaciones...");
			this.s = new ServerSocket(port);
			while( true ) {
				Socket sServicio = s.accept();
				System.out.println( "Aceptada conexion de " + sServicio.getInetAddress().toString() );
	            ClientHandler clientSock = new ClientHandler(sServicio);
	            new Thread(clientSock).start();
			}
            
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


//    public static void main(String[] args) {
//        //new NodoCentral();
//    	int puertoEnvioNodoControl = 5012; //RECIBE DE MULTISERVIDOR
//    	
//
//		Socket socketDerecha;
//		try {
//			socketDerecha = new Socket("localhost", puertoEnvioNodoControl);
//			ObjectOutputStream outputDerecha = new ObjectOutputStream(socketDerecha.getOutputStream());
//			PeticionDatos pd = new PeticionDatos("OP_CPU");
//			outputDerecha.writeObject(pd);
//			ObjectInputStream inputDerecha = new ObjectInputStream(socketDerecha.getInputStream());
//			System.out.println("Input creado");
//			PeticionDatos rc = (PeticionDatos)inputDerecha.readObject();
//			System.out.println(rc.getSubtipo());
//			
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			
//			e.printStackTrace();
//		}
//		
//    }
    public void main(String[] args) throws IOException {
		ServerSocket socketIzquierda = new ServerSocket(puertoRecepcion);
		Socket socketRecepcion;
		System.out.println("Bienvenido a la terminal cliente que controla la cadena de producción\n");
		try {
			
				boolean done = false;
				Socket socketEnvio = new Socket("localhost", puertoEnvio);
				ObjectOutputStream outputEnvio = new ObjectOutputStream(socketEnvio.getOutputStream());
				
				PeticionDatos p = new PeticionDatos("OP_CPU");
				
	            outputEnvio.writeObject(p);
	            
				while (!done) {
					socketRecepcion = socketIzquierda.accept();
					ObjectInputStream inputRecepcion = new ObjectInputStream(socketRecepcion.getInputStream());
					RespuestaControl rc = (RespuestaControl) inputRecepcion.readObject();
					
					
					if (rc.getSubtipo().equals("OK")) {
						System.out.println("La operación se ha completado con exito!");
						System.out.println(rc.getTokens() + "\n" + rc.getCpus());
						int menor = 99999999;
						int index = 0;
						for(int i=0; i < rc.getCpus().size(); i++) {
							if(rc.getCpus().get(i) < menor) index = i;
						}
						System.out.println("El de menor carga es: " + rc.getTokens().get(index));
					
						done = true;
						
					}
					if (inputRecepcion != null)
						inputRecepcion.close();
					if (socketRecepcion != null)
						socketRecepcion.close();
				}
			
		} catch (UnknownHostException ex) {
		} catch (IOException ex) {
			System.out.println(ex);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

    //--------------------------------------------------------------------------

    public void init() {       
        // Codigo de inicializacion ...    	
    }


    
    private static class ClientHandler extends Thread { 
		
	    private final Socket clientSocket; 
	    private ObjectInputStream is;
	    private ObjectOutputStream os;
	    private String PATHFILTERS = "/Users/rocioruizruiz/Documentos/Tercero/SistemasDistribuidos/Workspace/TrabajoFinalFiltros/src/main/resources/filters.txt"; //Tipos de filtros que hay para comprobar si existen. Local del NodoCentral
	    // Constructor 
	    public ClientHandler(Socket socket) 
	    { 
	        this.clientSocket = socket; 
	    } 
	
	    public void run() 
	    { 
	        try {               
	             // start 
	            procesaCliente(clientSocket);
	            
	        }catch (Error e) { 
	            e.printStackTrace(); 
	        } 
	        
	    }

	    //--------------------------------------------------------------------------

	    public void procesaCliente(Socket sServicio) {
	        try {
	        	this.is = new ObjectInputStream(sServicio.getInputStream());
	            this.os = new ObjectOutputStream(sServicio.getOutputStream());

	        	while(true) {
	            
		            
		            PeticionDatos pd = (PeticionDatos)this.is.readObject();
	
	            	if( pd.getSubtipo().compareTo("OP_FILTRO")==0 ) {
	                        this.doFiltering(pd);
	            	}
	        	}

	        } catch (ClassNotFoundException ex) {
	        } catch (IOException ex) {
	        } finally {
	            try {
	                os.close();
	                is.close();
	                sServicio.close();
	            } catch (SocketException ex) {
	            	System.out.println("Broken pipe! Finishing task...");
	            	System.out.println("Ready to use again!");
	            }
	            catch (IOException ex) {
	            	ex.printStackTrace();
	            }
	        }
	    }

	    //--------------------------------------------------------------------------

	    public void doFiltering(PeticionDatos pd) {
	        
	        String filtro = (String) pd.getFiltro();
	        File file = new File(PATHFILTERS);
	    	String last = "";
	    	boolean exists = false;
	    	try {
		    	if (file.exists()) { 
		    		BufferedReader br = new BufferedReader(new FileReader(file)); 
		    		last = br.readLine();
		    		while (last != null) { 
		    			if(last.equals(filtro)) {
		    				exists = true; break;
		    			}
			    		last = br.readLine(); 
		    		}
		    		br.close();
		    		if(exists) {	
		            	System.out.println("El filtro solicitado: " + filtro + " existe!");	            	
		            	RespuestaControl rc = new RespuestaControl("OK");
		            	sleep(25000);
		                this.os.writeObject(rc);  
		                
		                //Aqui deberia quedarse a la escuha del path y solicitar a multiservidores.
		                pd = (PeticionDatos)this.is.readObject();
		                
		                //Aqui se conectaria con el multiservidor despues de pedir las cpus y coger el menor, de momento me lo salto y con nodocontrol directamente
		                // solicitoCPUSyElijomenor();
		                // envioAlsolicitadoPeti();
		                
		                
		                this.os.writeObject("OK"); //cambiar ok por la respuesta del multiservidor
		                
		                
		                
		                
		                //
		            }else {
		            	System.out.println("El filtro solicitado: " + filtro + " NO existe!");	    
		            	RespuestaControl rc = new RespuestaControl("NOT_OK");
		                this.os.writeObject(rc);  
		            }
	    		} else { 
	    			
		    		System.out.println("No found file"); 
	    		}
	    	} catch (FileNotFoundException e) {
				e.printStackTrace();
	    	} catch (SocketException e) {
	    		e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();		
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
    }
}
