package apalabrados.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Match {
	private String id;
	private User playerA;
	private User playerB;
	private Map<String, Integer> scores;
	private User gameTurn;
	private Board board;
	private Timer timer;

	// Listado de casillas que están pendientes de ser confirmadas.
	// Esto es, cuando el servidor valida una jugada pero espera confirmación.
	private ArrayList<Square> pendingSquares;
	private MovementResult pendingMovement;

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
		this.timer = new Timer(120, this);
		this.pendingSquares = new ArrayList();
		this.scores = new HashMap<String, Integer>();
		this.scores.put(this.playerA.getSession().getId(), 0);
		this.scores.put(this.playerB.getSession().getId(), 0);
		this.board = new Board();
		this.gameTurn = new Random().nextBoolean() ? this.playerA : this.playerB;

		// Message to player A
		try {
			JSONObject jsa = new JSONObject();
			System.out.println(board.availableLetters());
			jsa.put("type", "START");
			jsa.put("letters", board.getLetters(7));
			// Mandamos las fichas restantes menos las que se le enviarán al jugador B
			jsa.put("availablePieces", board.availableLetters() - 7);
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
			jsb.put("letters", board.getLetters(7));
			jsb.put("availablePieces", board.availableLetters());
			jsb.put("turn", this.gameTurn == playerB ? true : false);
			jsb.put("opponent", this.playerA.getUserName());
			this.playerB.sendMessage(jsb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.timer.start();
	}

	public void playerPlays(String idSession, JSONArray jsaMovement) throws Exception {
		MovementResult result;
		User player;
		this.pendingMovement = null;

		if (this.playerA.getSession().getId().equals(idSession)) {
			player = playerA;
		} else {
			player = playerB;
		}

		if (player != this.gameTurn) {
			result = new MovementResult("RESULT");
			result.addException("No tienes el turno");
			player.sendMessage(result);
		} else {
			ArrayList<JSONObject> movement = new ArrayList<>();

			for (int i = 0; i < jsaMovement.length(); i++)
				movement.add(jsaMovement.getJSONObject(i));
			result = this.board.movement(movement);

			// Si alguna palabra no es correcta, el turno sigue siendo del jugador.
			System.out.println(result.toString());
			player.sendMessage(result);

			if (result.invalids() == 0 && result.exceptions() == 0) {
				// Guardar el movimiento pendiente
				this.pendingMovement = result;

				// Guardar las casillas por confirmar
				JSONObject jsoCasilla;
				Square square;
				for (int i = 0; i < jsaMovement.length(); i++) {
					jsoCasilla = jsaMovement.getJSONObject(i);
					square = new Square(jsoCasilla.getInt("row"), jsoCasilla.getInt("col"),
							jsoCasilla.getString("letter").charAt(0));
					this.pendingSquares.add(square);
				}
			}
		}
	}

	public void acceptMovement(String idSession) throws Exception {
		this.timer.stop();
		User player;
		User opponent;
		MovementResult result;

		if (this.playerA.getSession().getId().equals(idSession)) {
			player = playerA;
			opponent = playerB;
		} else {
			player = playerB;
			opponent = playerA;
		}
		if (player != this.gameTurn) {
			result = new MovementResult("RESULT");
			result.addException("No tienes el turno");
			player.sendMessage(result);

		} else if (this.pendingMovement == null) {
			result = new MovementResult("RESULT");
			result.addException("No hay jugadas pendientes de confirmación");
			player.sendMessage(result);

		} else {
			this.scores.put(player.getSession().getId(),
					(this.pendingMovement.getPoints() + this.scores.get(player.getSession().getId())));
			// Primero mandamos un mensaje al jugador que ha confirmado su jugada
			this.pendingMovement.setType("MOVEMENT");
			this.pendingMovement.setLetters(this.board.getLetters(this.pendingSquares.size()));
			this.pendingMovement.setScore(this.scores.get(player.getSession().getId()));
			this.pendingMovement.setAvailablePieces(this.board.availableLetters());
			player.sendMessage(this.pendingMovement);

			// Después, notificamos al jugador que estaba esperando
			this.pendingMovement.setType("OPPONENT_MOVEMENT");
			this.pendingMovement.setLetters("");
			this.pendingMovement.setAvailablePieces(this.board.availableLetters());
			this.pendingMovement.setScore(this.scores.get(player.getSession().getId()));
			opponent.sendMessage(this.pendingMovement);

			// Cambiar turno
			this.gameTurn = (this.playerA == this.gameTurn ? this.playerB : this.playerA);
			this.pendingMovement = null;
			this.pendingSquares.clear();
		}

		this.timer.start();

	}

	public void toggleTurn(String idSession) throws Exception {
		this.timer.stop();
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
			movement = new MovementResult("MOVEMENT");
			movement.addException("No tienes el turno");
			player.sendMessage(movement);
		} else {
			// Primero mandamos un mensaje al jugador que ha pedido cambiar el turno
			// confirmándole
			this.gameTurn = (this.playerA == this.gameTurn ? this.playerB : this.playerA);
			movement = new MovementResult(10, board.availableLetters());
			player.sendMessage(movement);

			// Después, notificamos al jugador que estaba esperando
			movement = new MovementResult(10, board.availableLetters());
			movement.setType("OPPONENT_MOVEMENT");
			opponent.sendMessage(movement);
		}
		this.timer.start();
	}

	public void giveUp(String idSession) {
		this.timer.stop();
		User player; // Loser
		User opponent; // Winner

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
		
		this.endMatch();
	}

	public void changeLetters(String idSession, Character[] letters) {
		User player = this.playerA.getSession().getId().equals(idSession) ? playerA : playerB;

		try {
			JSONObject jsa = new JSONObject();
			jsa.put("type", "NEW_LETTERS");
			jsa.put("letters", board.getLetters(letters.length));

			// Devolver las letras del usuario al listado de letras
			for (Character letter : letters) {
				this.board.addLetter(letter);
			}
			player.sendMessage(jsa.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {

		}
	}

	public void expiredTime() {
		User player;
		User opponent;
		MovementResult result;

		if (this.playerA == this.gameTurn) {
			player = playerA;
			opponent = playerB;
		} else {
			player = playerB;
			opponent = playerA;
		}
		
		// Primero mandamos un mensaje al jugador que tenía el turno
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
		
		this.endMatch();
	}
	
	private void endMatch() {
		this.timer.kill();
	}
	

}
