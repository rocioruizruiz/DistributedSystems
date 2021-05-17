package admins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Admin1 {

	private static void listPath(File path) {
		String[] pathnames;
		File f = path;
		pathnames = f.list();
		for (String pathname : pathnames) {
			System.out.println(pathname);
		}
	}

	static void purgeDirectory(File dest) {
		for (File file : dest.listFiles()) {
			if (file.isDirectory())
				purgeDirectory(file);
			file.delete();
		}
	}

	private static void modifyFilter(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
			System.out.println("New filter saved!");
		}
	}

	public static void main(String[] args) {
		System.out.println("Arrancando el nodo Admin 1...");
		while (true) {
			boolean ringNum = false;
			File path = new File("");
			System.out.println("\nIndica que anillo deseas modificar");
			System.out.println("1 / 2 / 3 ");
			Scanner sc = new Scanner(System.in);
			String anillo = sc.next();

			if (anillo.equals("1"))
				path = new File("/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/java/anillo/anillo1/filter");
			if (anillo.equals("2"))
				path = new File("/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/java/anillo/anillo2/filter");
			if (anillo.equals("3"))
				path = new File("/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/java/anillo/anillo3/filter");

			ringNum = true;

			while (ringNum) {
				System.out.println("\nIndica operacion que deseas hacer");
				System.out.println("list / modify / exit");
				String op = sc.next();

				if (op.equals("list")) {
					listPath(path);
				} else if (op.equals("modify")) {
					System.out.println("Localizacion de filtro nuevo a añadir");
					String filter = sc.next();
					File source = new File(filter);
					String[] filterPath = filter.split("/");
					String filterName = filterPath[filterPath.length - 1];
					File dest = new File(path + "/" + filterName);
					try {
						// Primero eliminamos el filtro anterior
						purgeDirectory(path);
						// Despues copiamos el nuevo filtro
						modifyFilter(source, dest);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (op.equals("exit")) {
					sc.close();
					System.out.println("Saliendo de nodo Admin 1...");
					System.exit(0);
				} else {
					System.out.println("Por favor, indica una operacion existente");
				}
			}
		}
	}
}
