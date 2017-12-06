package model;

public class Anime {
	int id;
	String nombre;
	String sinopsis;
	String genero1;
	String genero2;
	String genero3;
	String tipo;
	String imagen;
	int visitas;
	
	public Anime(int id, String n, String s, String g1, String g2,
			String g3, String t, String i, int v){
		this.id = id;
		this.nombre = n;
		this.sinopsis = s;
		this.genero1 = g1;
		this.genero2 = g2;
		this.genero3 = g3;
		this.tipo = t;
		this.imagen = i;
		this.visitas = v;
	}
	
	public int getVisitas() {
		return visitas;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getSinopsis() {
		return sinopsis;
	}

	public void setSinopsis(String sinopsis) {
		this.sinopsis = sinopsis;
	}

	public String getGenero1() {
		return genero1;
	}

	public void setGenero1(String genero1) {
		this.genero1 = genero1;
	}

	public String getGenero2() {
		return genero2;
	}

	public void setGenero2(String genero2) {
		this.genero2 = genero2;
	}

	public String getGenero3() {
		return genero3;
	}

	public void setGenero3(String genero3) {
		this.genero3 = genero3;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getId() {
		return id;
	}
	
	
}
