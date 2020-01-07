package apalabrados.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovementResult {
	private List<String> exceptions;
	private List<Cadena> valid;
	private List<Cadena> invalid;
	private int points;
	private int score;
	private String type;
	private int availablePieces;
	private String newLetters;
	
	public MovementResult(int score, int pieces) {
		this.exceptions = new ArrayList<>();
		this.points= 0;
		this.score = score;
		this.valid = new ArrayList<>();
		this.invalid = new ArrayList<>();
		this.availablePieces = pieces;
		this.type = "MOVEMENT";
		this.newLetters = "";
	}
	
	public MovementResult(String type) {
		this.exceptions = new ArrayList<>();
		this.points= 0;
		this.score = 0;
		this.valid = new ArrayList<>();
		this.invalid = new ArrayList<>();
		this.availablePieces = 0;
		this.type = type;
		this.newLetters = "";
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAvailablePieces(int availablePieces) {
		this.availablePieces = availablePieces;
	}
	
	public void setLetters(String letters) {
		this.newLetters = letters;
	}

	public int getPoints() {
		int r=0;
		for(int i=0; i<this.valid.size(); i++) {
			r+=this.valid.get(i).getPoints();
		}
		return r;
	}
	
	
	public List<String> getExceptions() {
		return exceptions;
	}

	public void addException(String message) {
		this.exceptions.add(message);
	}
	
	public int invalids() {
		return this.invalid.size();
	}
	
	public int valids() {
		return this.valid.size();
	}
	
	public int exceptions() {
		return this.exceptions.size();
	}

	public void addNotAccepted(Cadena cadena) {
		this.invalid.add(cadena);
	}

	public void addAccepted(Cadena cadena) {
		this.valid.add(cadena);
	}

	public boolean accepts(String... palabras) {
		for (String palabra : palabras) {
			if (!in(palabra, this.valid))
				return false;
		}
		return true;
	}	
	
	public boolean notAccepts(String... palabras) {
		for (String palabra : palabras) {
			if (!in(palabra, this.invalid))
				return false;
		}	
		return true;
	}

	private boolean in(String palabra, List<Cadena> cadenas) {
		for (Cadena cadena : cadenas)
			if (cadena.getText().equals(palabra))
				return true;
		return false;
	}

	public boolean acceptsAll() {
		return invalid.isEmpty();
	}


	public JSONObject toJSON() throws JSONException {
		JSONObject jso = new JSONObject();
		JSONArray jsaExceptions=new JSONArray();
		for (String ex : exceptions)
			jsaExceptions.put(ex);
		jso.put("exceptions", jsaExceptions);
		
		JSONArray jsaValid=new JSONArray();
		for (Cadena cadena : valid)
			jsaValid.put(cadena.toJSON());
		jso.put("valid", jsaValid);
		JSONArray jsaInvalid=new JSONArray();
		for (Cadena cadena : invalid)
			jsaInvalid.put(cadena.toJSON());
		jso.put("invalid", jsaInvalid);
		
		jso.put("score", score);
		jso.put("points", points);
		jso.put("letters", newLetters);
		jso.put("availablePieces", availablePieces);
		jso.put("type", type);
		jso.put("timestamp", new Date().getTime());
		
		return jso;
	}

	public void setPoints(int points) {
		this.points = points;
	}
}

