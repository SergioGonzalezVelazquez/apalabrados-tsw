package apalabrados.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovementResult {
	private List<String> exceptions;
	private int score;
	private String type;
	private List<String> movements;
	private int availablePieces;
	private boolean turn;
	
	public MovementResult(int score, int pieces) {
		this.exceptions = new ArrayList<>();
		this.score = score;
		this.movements = new ArrayList<>();
		this.availablePieces = pieces;
		this.type = "MOVEMENT";
		this.turn = false;
	}
	
	public MovementResult() {
		this.exceptions = new ArrayList<>();
		this.score = 0;
		this.movements = new ArrayList<>();
		this.availablePieces = 0;
		this.type = "movement";
		this.turn = false;
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

	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	public int getPoints() {
		int r=0;
		return r;
	}
	
	
	public List<String> getExceptions() {
		return exceptions;
	}

	public void addException(String message) {
		this.exceptions.add(message);
	}


	public JSONObject toJSON() throws JSONException {
		JSONObject jso = new JSONObject();
		JSONArray jsaExceptions=new JSONArray();
		for (String ex : exceptions)
			jsaExceptions.put(ex);
		
		JSONArray jsaMovements=new JSONArray();
		for (String mov : movements)
			jsaMovements.put(mov);
		
		jso.put("exceptions", jsaExceptions);
		jso.put("movements", jsaMovements);
		jso.put("score", score);
		jso.put("availablePieces", availablePieces);
		jso.put("type", type);
		jso.put("turn", turn);
		return jso;
	}
}

