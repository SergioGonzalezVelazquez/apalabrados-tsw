package apalabrados.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.UUID;

public class Match implements LetterDistribution {
	private String id;
	private User playerA;
	private User playerB;
	private User gameTurn;
	private Board board;

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

		// Message to player A
		try {
			JSONObject jsa = new JSONObject();
			jsa.put("type", "START");
			jsa.put("letters", getLetters(7));
			jsa.put("turn", this.gameTurn == playerA ? true : false);
			jsa.put("opponent", this.playerB.getUserName());
			this.playerA.sendMessage(jsa.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {

		}

		// Message to player B
		try {
			JSONObject jsb = new JSONObject();
			jsb.put("type", "START");
			jsb.put("letters", getLetters(7));
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
		for (int i = 0; i < n; i++)
			r = r + this.letters.remove(0) + " ";

		// Remove last space character
		r = r.substring(0, r.length() - 1);
		return r;
	}

	public void playerPlays(String idSession, JSONArray jsaJugada) throws Exception {

	}

	public void toggleTurn(String idSession) throws Exception {
		MovementResult movement;
		User player;
		User opponent;

		if (this.playerA.getSession().getId().equals(idSession)) {
			player = playerA;
			opponent = playerB;
		} else {
			player = playerB;
			opponent = playerA;
		}

		if (player != this.gameTurn) {
			movement = new MovementResult();
			movement.addException("No tienes el turno");
			player.sendMessage(movement);
		} else {
			// Primero mandamos un mensaje al jugador que ha pedido cambiar el turno
			// confirmándole
			this.gameTurn = (this.playerA == this.gameTurn ? this.playerB : this.playerA);
			movement = new MovementResult(10, letters.size());
			movement.setType("MOVEMENT");
			movement.setTurn(false);
			player.sendMessage(movement);

			// Después, notificamos al jugador que estaba esperando
			movement = new MovementResult(10, letters.size());
			movement.setType("OPPONENT_MOVEMENT");
			movement.setTurn(true);
			opponent.sendMessage(movement);
		}

	}

	public void giveUp(String idSession)  {
		User player; //Loser
		User opponent; //Winner
		
		if (this.playerA.getSession().getId().equals(idSession)) {
			player = playerA;
			opponent = playerB;
		} else {
			player = playerB;
			opponent = playerA;
		}
		
		// Primero mandamos un mensaje al jugador que se ha rendido
		try {
			JSONObject jsLoser = new JSONObject();
			jsLoser.put("winner", false);
			jsLoser.put("type", "MATCH_END");
			player.sendMessage(jsLoser.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Después, mandamos el mensaje al jugador que ha ganado
		try {
			JSONObject jsWinner = new JSONObject();
			jsWinner.put("winner", true);
			jsWinner.put("type", "MATCH_END");
			opponent.sendMessage(jsWinner.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public void changeLetters (String idSession, Character [] letters) {
		User player = this.playerA.getSession().getId().equals(idSession) ? playerA : playerB;
		
		try {
			JSONObject jsa = new JSONObject();
			jsa.put("type", "NEW_LETTERS");
			jsa.put("letters", getLetters(letters.length));
			
			//Devolver las letras del usuario al listado de letras
			for (Character letter : letters)
			{ 
			    this.letters.add(letter);
			}
			player.sendMessage(jsa.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {

		}
	}

}
