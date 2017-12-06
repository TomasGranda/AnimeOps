package model;

public class Cancion {
	
	int id;
	String nombre;
	String tipo;
	String banda;
	String anime;
	int descargas;
	String usuario;
	
	public Cancion(int id, String nombre, String tipo, String banda, String anime, int descargas,String us) 
	{
		this.id = id;
		this.nombre = nombre;
		this.tipo = tipo;
		this.banda = banda;
		this.anime = anime;
		this.descargas = descargas;
		this.usuario = us;
	}

	public String getUsuario() {
		return usuario;
	}
	
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getBanda() {
		return banda;
	}

	public void setBanda(String banda) {
		this.banda = banda;
	}

	public String getAnime() {
		return anime;
	}

	public void setAnime(String anime) {
		this.anime = anime;
	}

	public int getDescargas() {
		return descargas;
	}

	public void setDescargas(int descargas) {
		this.descargas = descargas;
	}

	public int getId() {
		return id;
	}
	
	
	
}
