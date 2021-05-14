package admins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	private static void newFilter(File source, File dest) throws IOException {
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

	private static void deleteFilter(Path path) {
		try {
			Files.delete(path);
			System.out.println("Filter deleted");
		} catch (NoSuchFileException x) {
			System.out.println("No such file/directory exists");
		} catch (DirectoryNotEmptyException x) {
			System.out.println("Directory is not empty.");
		} catch (IOException x) {
			System.out.println("Invalid permissions.");
		}
	}

	public static void main(String[] args) {
		System.out.println("Arrancando el nodo Admin 1...");
		while (true) {
			File path = new File("");
			System.out.println("\nIndica que anillo deseas modificar");
			System.out.println("1 / 2 / 3 ");
			Scanner sc = new Scanner(System.in);
			String anillo = sc.next();

			if (anillo.equals("1"))
				path = new File("/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/java/anillo/anillo1");
			if (anillo.equals("2"))
				path = new File("/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/java/anillo/anillo2");
			if (anillo.equals("3"))
				path = new File("/home/agus/eclipse-workspace/TrabajoFinalSistB/src/main/java/anillo/anillo3");

			System.out.println("\nIndica operacion que deseas hacer");
			System.out.println("list / new / delete / exit");
			String op = sc.next();

			if (op.equals("list")) {
				listPath(path);
			} else if (op.equals("new")) {
				System.out.println("Localizacion de filtro nuevo a añadir");
				String filter = sc.next();
				File source = new File(filter);
				String[] filterPath = filter.split("/");
				String filterName = filterPath[filterPath.length - 1];
				File dest = new File(path + "/" + filterName);
				try {
					newFilter(source, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (op.equals("delete")) {
				System.out.println("Nombre del archivo a borrar");
				String filter = sc.next();
				Path delete = Paths.get(path + "/" + filter);
				deleteFilter(delete);
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
