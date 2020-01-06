package apalabrados.model;

public class Square {
	private char letter;
	private Type type;
	private boolean provisional;
	
	Square() {
		this.letter='\0';
		this.type=Type.NORMAL;
		this.provisional = true;
	}

	public void setType(Type type) {
		this.type=type;
	}
	
	public Type getType() {
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
}
