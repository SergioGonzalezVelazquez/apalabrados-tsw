package apalabrados.model;

public class Letter {
	private char letter;
	private int points;
	
	public Letter(char c, int points) {
		this.letter = c;
		this.points = points;
	}

	public char getLetter() {
		return this.letter;
	}

	public int getPoints() {
		return points;
	}
}
