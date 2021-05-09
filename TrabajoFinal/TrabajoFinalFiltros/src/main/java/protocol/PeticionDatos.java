package protocol;

import java.util.ArrayList;

import server.Pair;

public class PeticionDatos extends Peticion {
	private ArrayList args;
	private String filtro;
	private String nodoanillo;
	private String path;
	private ArrayList<String> tokens;
	private ArrayList<Double> cpus;

	// --------------------------------------------------------------------------

	public PeticionDatos() {
		this.tipo = "PETICION_DATOS";
		this.args = new ArrayList();
		this.path = new String();
		this.filtro = new String();
		this.cpus = new ArrayList<Double>();
		this.tokens = new ArrayList<String>();
		this.nodoanillo = new String();

	}

	// --------------------------------------------------------------------------

	public PeticionDatos(String subtipo) {
		this.tipo = "PETICION_DATOS";
		this.subtipo = subtipo;
		this.args = new ArrayList();
		this.path = new String();
		this.filtro = new String();
		this.cpus = new ArrayList<Double>();
		this.tokens = new ArrayList<String>();
		this.nodoanillo = new String();

	}

	// --------------------------------------------------------------------------

	public ArrayList getArgs() {
		return args;
	}

	// --------------------------------------------------------------------------

	public void setArgs(ArrayList args) {
		this.args = args;
	}

	// --------------------------------------------------------------------------

	public String getFiltro() {
		return filtro;
	}

	// --------------------------------------------------------------------------

	public void setFiltro(String filtro) {
		this.filtro = filtro;
	}

	// --------------------------------------------------------------------------

	public String getPath() {
		return path;
	}

	// --------------------------------------------------------------------------

	public void setPath(String path) {
		this.path = path;
	}

	// --------------------------------------------------------------------------

	public ArrayList<String> getTokens() {
		return tokens;
	}

	// --------------------------------------------------------------------------

	public void setTokens(ArrayList<String> tokens) {
		this.tokens = tokens;
	}

	// --------------------------------------------------------------------------

	public ArrayList<Double> getCpus() {
		return cpus;
	}

	// --------------------------------------------------------------------------

	public void setCpus(ArrayList<Double> cpus) {
		this.cpus = cpus;
	}

	// --------------------------------------------------------------------------

	public String getNodoanillo() {
		return nodoanillo;
	}

	// --------------------------------------------------------------------------

	public void setNodoanillo(String nodoanillo) {
		this.nodoanillo = nodoanillo;
	}

}