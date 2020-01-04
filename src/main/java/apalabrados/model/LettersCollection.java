package apalabrados.model;

import java.util.ArrayList;
import java.util.List;

public class LettersCollection {
	private List<Letter> letters = new ArrayList<>();

	public Letter remove(int index) {
		return this.letters.remove(index);
	}

	public void add(int amount, char c, int points) {
		for (int i=0; i<amount; i++)
			this.letters.add(new Letter(c, points));
	}

	public int size() {
		return this.letters.size();
	}

	public Letter get(int index) {
		return this.letters.get(index);
	}

	public void set(int index, Letter letter) {
		this.letters.set(index, letter);
	}

}
