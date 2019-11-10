package apalabrados.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Match implements LetterDistribution {
	private String id;
	private User playerA;
	private User playerB;
	private User gameTurn;

	private ArrayList<Character> letters = new ArrayList();

	public Match() {
		this.id = UUID.randomUUID().toString();
	}

	public void setPlayerA(User user) {
		this.playerA = user;
	}

	public void setPlayerB(User playerB) {
		this.playerB = playerB;
	}

	public String getId() {
		return id;
	}
	
	public void start() {
		this.initializeLetters();
		this.gameTurn = new Random().nextBoolean() ? this.playerA : this.playerB;
		
		try {
			JSONObject jsa = new JSONObject();
			jsa.put("type", "START");
			jsa.put("letters", getLetters(7) + " player a");
			jsa.put("turn", this.gameTurn == playerA ? true : false);
			jsa.put("opponent", this.playerB.getUserName());
			this.playerA.sendMessage(jsa.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			
		}
		try {
			JSONObject jsb = new JSONObject();
			jsb.put("type", "START");
			jsb.put("letters", getLetters(7) + " player b");
			jsb.put("turn", this.gameTurn == playerB ? true : false);
			jsb.put("opponent", this.playerA.getUserName());
			this.playerB.sendMessage(jsb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void initializeLetters() {
		for (Entry<Character, Integer> entry : LETTER_QUANTITY.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				letters.add(entry.getKey());
			}
		}
		// Shuffle letras
		Collections.shuffle(this.letters);
	}
	
	private String getLetters(int n) {
		String r = "";
		for(int i=0; i<n; i++)
			r = r + this.letters.remove(0) + " ";
		return r;
	}


}
