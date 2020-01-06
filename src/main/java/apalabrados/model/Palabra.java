package apalabrados.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Palabra {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	private String texto;
	
	public Palabra() {
	}
	
	public Palabra(String texto) {
		setTexto(texto);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}
	
	
}
