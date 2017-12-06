package model;

public class Contacto
{
	int id;
	String nombre;
	String email;
	String comentario;
	
	public Contacto(int id, String nombre, String email, String comentario)
	{
		this.id = id;
		this.comentario = comentario;
		this.email = email;
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public int getId() {
		return id;
	}
	
	
	
	
}