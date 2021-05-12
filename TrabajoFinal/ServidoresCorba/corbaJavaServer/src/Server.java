import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import FilterApp.Filter;
import FilterApp.FilterHelper;
import FilterApp.FilterPOA;

class FilterImpl extends FilterPOA {
	private ORB orb;
	private int n = 1;

	public String applyFilter(String filtro, String imgpath) {
		File originalPath = new File(imgpath);
		String imgName = imgpath.substring(imgpath.lastIndexOf('/')+1, imgpath.lastIndexOf('.'));
		String format = imgpath.substring(imgpath.lastIndexOf('.')+1, imgpath.length());
		String outputPath = "/Volumes/filteredImages/" + imgName + n + ".jpg";
		try {
			BufferedImage image = ImageIO.read(originalPath);
			
			if (filtro.equals("GrayScale")) {
				BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
						BufferedImage.TYPE_BYTE_GRAY);

				Graphics2D graphics = result.createGraphics();
				graphics.drawImage(image, 0, 0, null);
				graphics.dispose();

				
				File output = new File(outputPath); 
				ImageIO.write(result, "jpg", output);
				System.out.println(output.getPath());
				System.out.println("Done!"); n++;
				return outputPath;
			} else if (filtro.equals("BandW")) {
				BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
						BufferedImage.TYPE_BYTE_BINARY);

				Graphics2D graphic = result.createGraphics();
				graphic.drawImage(image, 0, 0, Color.WHITE, null);
				graphic.dispose();

				File output = new File(outputPath); 
				ImageIO.write(result, "jpg", output);
				System.out.println(output.getPath());
				System.out.println("Done!"); n++;
				return outputPath;
			} else if (filtro.equals("Sature")) {
				BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);

				Graphics2D graphics = result.createGraphics();
				Color newColor = new Color(30, 30, 30, 0);
				graphics.setXORMode(newColor);
				graphics.drawImage(image, 0, 0, null);
				graphics.dispose();

				File output = new File(outputPath); // Aqui iria la carpeta de destino NFS
				ImageIO.write(result, "jpg", output);
				System.out.println(output.getPath());
				System.out.println("Done!"); n++;
				return outputPath;
			} else {
				System.out.println("ERROR. FILTER NOT FOUND.");
				return "Se ha producido un error";
			}
			
		} catch(IIOException e) {
			System.out.println("Can`t read input file");
			return "Can`t read input file";
		}
		catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		
	}

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
}

public class Server {

	public static void main(String[] args) {

		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			FilterImpl filterImpl = new FilterImpl();
			filterImpl.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(filterImpl);
			Filter href = FilterHelper.narrow(ref);

			// get the root naming context
			// NameService invokes the name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			System.out.println("Finding naming service...");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			System.out.println("Naming service found");

			// bind the Object Reference in Naming
			String name = "Filter";
			NameComponent path[] = ncRef.to_name(name);
			try {
				// Bins the new naming reference to the object
				ncRef.rebind(path, href);
			} catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
				System.out.println("Object not found...");
				System.exit(0);
			} catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
				System.out.println("Error. Server cannot proceed...");
				System.exit(0);
			}

			System.out.println("Corba Server ready and waiting ...");
			while (true) {
				orb.run();
			}

		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		System.out.println("Corba Server Exiting ...");
	}
}