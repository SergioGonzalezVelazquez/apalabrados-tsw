package apalabrados.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Casilla del tablero
public class Square {
	private char letter;
	private int row;
	private int col;
	private SquareType type;
	private boolean provisional;
	
	Square(int row, int col) {
		this.letter='\0';
		this.row = row;
		this.col = col;
		this.type=SquareType.NORMAL;
		this.provisional = true;
	}
	
	Square(int row, int col, char letter) {
		this.letter=letter;
		this.row = row;
		this.col = col;
	}
	
	Square() {
		this.row = -1;
		this.col = -1;
		this.letter='\0';
		this.type=SquareType.NORMAL;
		this.provisional = true;
	}
	

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public void setType(SquareType type) {
		this.type=type;
	}
	
	public SquareType getType() {
		return type;
	}
	
	public char getLetter() {
		return letter;
	}

	public void setLetter(char letter) {
		this.letter = letter;
		this.provisional = true;
	}
	
	public void setProvisional(boolean provisional) {
		this.provisional = provisional;
	}
	
	public boolean isProvisional() {
		return provisional;
	}

	public boolean isEmpty() {
		return this.letter=='\0';
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject jso = new JSONObject();
		jso.put("col", this.col);
		jso.put("row", this.row);
		jso.put("letter", Character.toString(letter));
		
		return jso;
	}
}
