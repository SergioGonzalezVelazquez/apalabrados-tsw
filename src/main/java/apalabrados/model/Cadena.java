package apalabrados.model;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Cadena {
	private ArrayList<Square> squares;
	private int points;
	
	public Cadena() {
		this.squares=new ArrayList<>();
	}

	public void add(Square square) {
		this.squares.add(square);
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder("(");
		for (Square square : squares)
			sb.append(square.getLetter());
		sb.append(", " + this.points + ") ");
		return sb.toString();
	}

	public int length() {
		return squares.size();
	}

	public void calculatePoints() {
		int dp=0, tp=0;
		for (Square square : this.squares) {
			if (this.squares.size()>1) {
				char letra = square.getLetter();
				switch (square.getType()) {
					case NORMAL :
						this.points+=Board.puntos.get(letra);
						break;
					case DL : 
						if (square.isProvisional())
							this.points+=2*Board.puntos.get(letra);
						else
							this.points+=Board.puntos.get(letra);
						break;
					case TL : 
						if (square.isProvisional())
							this.points+=3*Board.puntos.get(letra);
						else
							this.points+=Board.puntos.get(letra);
						break;
					case DP:
						this.points+=Board.puntos.get(letra);
						if (square.isProvisional())
							dp++;
						break;
					case TP:
						this.points+=Board.puntos.get(letra);
						if (square.isProvisional())
							tp++;
						break;
					default:
						break;
				}
			}
		}
		for (int i=0; i<dp; i++)
			this.points*=2;
		for (int i=0; i<tp; i++)
			this.points*=3;
	}

	public String getText() {
		StringBuilder sb=new StringBuilder();
		for (Square square : squares)
			sb.append(square.getLetter());
		return sb.toString();
	}

	public int getPoints() {
		return this.points;
	}

	public void setProvisional(boolean provisional) {
		for (Square square : squares)
			square.setProvisional(provisional);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject jso = new JSONObject();
		jso.put("sequence", this.getText());
		jso.put("points", this.getPoints());
		return jso;
	}
}
