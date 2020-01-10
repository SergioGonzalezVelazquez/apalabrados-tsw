package apalabrados.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import apalabrados.dao.MatchRepository;
import apalabrados.web.controllers.Manager;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "matches")
public class Match {
	@Id
	private String id;

	@Column
	private MatchStatus status;

	@OneToOne
	@JoinColumn(unique = false, name = "PLAYER_A")
	private User playerA;
	@Column(name = "PLAYER_A_WINS")
	private boolean playerAWins;

	@OneToOne
	@JoinColumn(unique = false, name = "PLAYER_B")
	private User playerB;
	@Column(name = "PLAYER_B_WINS")
	private boolean playerBWins;

	@Column(name = "created_at")
	private long created;

	@Transient
	private Map<String, Integer> scores;
	@Transient
	private User gameTurn;
	@Transient
	private Board board;
	@Transient
	private Timer timer;
	@Transient
	private MatchRepository matchRepo;

	// Flag utilizado para colocar las letras en un determinado orden y
	// realizar una serie de movimientos planeados en los tests. También
	// se utilza para no generar el turno inicial de manera aleatoria y
	// asignarselo al player A.
	@Transient
	private boolean testing = false;

	// Listado de casillas que están pendientes de ser confirmadas.
	// Esto es, cuando el servidor valida una jugada pero espera confirmación.
	@Transient
	private int pendingLetters;
	@Transient
	private MovementResult pendingMovement;

	private HashMap<String, Integer> letters;

	public Match() {
		this.id = UUID.randomUUID().toString();
		this.matchRepo = Manager.get().getMatchRepo();
		this.status = MatchStatus.WAITING;
		this.playerAWins = false;
		this.playerBWins = false;
		this.created = System.currentTimeMillis();
	}

	public void setPlayerA(User user) {
		this.playerA = user;
	}

	public void setTesting(boolean testing) {
		this.testing = testing;
	}

	public void setPlayerB(User playerB) {
		this.playerB = playerB;
	}

	public MatchStatus getStatus() {
		return status;
	}

	public void setStatus(MatchStatus status) {
		this.status = status;
	}

	public User getPlayerA() {
		return playerA;
	}

	public boolean isPlayerAWins() {
		return playerAWins;
	}

	public User getPlayerB() {
		return playerB;
	}

	public boolean isPlayerBWins() {
		return playerBWins;
	}

	public long getCreated() {
		return created;
	}

	public String getId() {
		return id;
	}

	public void start() throws JSONException {
		this.status = MatchStatus.IN_PLAY;
		this.timer = new Timer(120, this);
		this.pendingLetters = 0;
		this.scores = new HashMap<String, Integer>();
		this.scores.put(this.playerA.getSession().getId(), 0);
		this.scores.put(this.playerB.getSession().getId(), 0);
		this.letters = new HashMap<String, Integer>();
		this.letters.put(this.playerA.getSession().getId(), 7);
		this.letters.put(this.playerB.getSession().getId(), 7);
		
		if (this.testing) {
			this.board = new Board(true);
			this.gameTurn = this.playerA;
		} else {
			this.board = new Board();
			this.gameTurn = new Random().nextBoolean() ? this.playerA : this.playerB;
		}

		// Message to player A
		try {
			JSONObject jsa = new JSONObject();
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
		this.matchRepo.save(this);
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
			player.sendMessage(result);

			if (result.invalids() == 0 && result.exceptions() == 0) {
				// Guardar el movimiento pendiente
				this.pendingMovement = result;

				// Guardar el número de letras por confirmar
				this.pendingLetters = jsaMovement.length();
			}
		}
		// Quita las letras del tablero hasta que se confirme la jugada
		this.board.quitarJugadaProvisional();
	}

	public void acceptMovement(String idSession) throws Exception {
		this.timer.stop();
		this.board.confirmarJugada();
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
			
	
			this.pendingMovement.setLetters(this.getLetters(player, this.pendingLetters));
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
			this.pendingLetters = 0;
		}
		
		if(this.board.availableLetters() == 0 && this.letters.get(player.getSession().getId()) == 0) {
			//El juego ha terminado. Comprobamos quien ha ganado
			
			boolean winner = this.scores.get(player.getSession().getId()) > this.scores.get(opponent.getSession().getId());
			
			if(winner) {
				this.sendWinnerNotification(player);
				this.sendLoserNotification(opponent);
				if (this.playerA.getSession().getId().equals(idSession)) {
					this.playerAWins = true;
				}
				else {
					this.playerBWins = true;
				}
			}
			else {
				this.sendWinnerNotification(opponent);
				this.sendLoserNotification(player);
				
				if (this.playerA.getSession().getId().equals(idSession)) {
					this.playerBWins = true;
				}
				else {
					this.playerAWins = true;
				}
				
			}
			
			this.endMatch();
			
		}
		else {
			this.timer.start();
		}
	}
	
	private void sendWinnerNotification (User winner) throws JSONException {
		try {
			JSONObject jsWinner = new JSONObject();
			jsWinner.put("winner", true);
			jsWinner.put("type", "MATCH_END");
			winner.sendMessage(jsWinner.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Excepción al enviar mensaje en rendirse al ganador");
		}
	}
	
	private void sendLoserNotification (User loser) throws JSONException {
		try {
			JSONObject jsLoser = new JSONObject();
			jsLoser.put("winner", false);
			jsLoser.put("type", "MATCH_END");
			loser.sendMessage(jsLoser.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Excepción al enviar mensaje en rendirse al perdedor");
		}
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

	public void giveUp(String idSession) throws JSONException {
		this.timer.stop();
		User player; // Loser
		User opponent; // Winner

		if (this.playerA.getSession().getId().equals(idSession)) {
			player = playerA;
			playerBWins = true;
			opponent = playerB;
		} else {
			player = playerB;
			playerAWins = true;
			opponent = playerA;
		}

		// Primero mandamos un mensaje al jugador que se ha rendido
		this.sendLoserNotification(player);

		// Después, mandamos el mensaje al jugador que ha ganado
		this.sendWinnerNotification(opponent);

		this.endMatch();
	}

	public void changeLetters(String idSession, Character[] letters) throws Exception {
		User player = this.playerA.getSession().getId().equals(idSession) ? playerA : playerB;
		
		String nuevasLetras = this.board.changeLetters(letters);
		
		try {
			JSONObject jsa = new JSONObject();
			jsa.put("type", "NEW_LETTERS");
			jsa.put("letters", nuevasLetras);
			player.sendMessage(jsa.toString());

		} catch (IOException e) {
			System.out.println("Excepción al enviar mensaje en changeLetters");
		} catch (JSONException e) {

		}
	}

	public void expiredTime() throws JSONException {
		User player;
		User opponent;

		if (this.playerA == this.gameTurn) {
			player = playerA;
			opponent = playerB;
			playerBWins = true;
		} else {
			player = playerB;
			opponent = playerA;
			playerAWins = true;
		}

		// Primero mandamos un mensaje al jugador que tenía el turno
		this.sendLoserNotification(player);

		// Después, mandamos el mensaje al jugador que ha ganado
		this.sendWinnerNotification(opponent);


		this.endMatch();
	}

	// Un jugador se desconecta
	public void logout(String idSession) throws JSONException {
		this.timer.stop();
		User winner;

		if (this.playerA.getSession().getId().equals(idSession)) {
			winner = playerB;
			playerBWins = true;
		} else {
			winner = playerA;
			playerAWins = true;
		}
		
		this.sendWinnerNotification(winner);
		this.endMatch();

	}

	private void endMatch() {
		this.timer.kill();

		// Decirle al Controller que la borre de partidas en juego
		Manager.get().endMatch(this.id);

		this.status = MatchStatus.FINISHED;
		this.matchRepo.save(this);
	}
	
	//Pide al tablero nuevas letras para un determinado jugador.
	//Actualiza el número de letras que tiene ese jugador como
	//la diferencia entre el número de letras jugadas y el número
	//de letras recibidas por el tablero
	private String getLetters(User player, int jugadas) {
		
		String newLetters = this.board.getLetters(jugadas);
		
		int nuevas;
		
		if(newLetters.equals("")) 
			nuevas = 0;
		else
			nuevas = newLetters.split(" ").length;
		
		System.out.println(player.getUserName() + " tenía " +  this.letters.get(player.getSession().getId()) + "letras y juega " + jugadas);
		System.out.println("El servidor le da " + nuevas);
		
		this.letters.put(player.getSession().getId(), (this.letters.get(player.getSession().getId()) - jugadas + nuevas));
		
		System.out.println("a " + player.getUserName() + " le quedan " + this.letters.get(player.getSession().getId()) + " letras");
		
		return newLetters;
	}

}
