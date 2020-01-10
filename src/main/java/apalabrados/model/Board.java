package apalabrados.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import apalabrados.dao.PalabraRepository;
import apalabrados.dao.UserRepository;
import apalabrados.web.controllers.Manager;

public class Board implements LetterDistribution {
	static Map<Character, Integer> puntos = LETTER_VALUE;
	private Square[][] squares = new Square[15][15];
	private ArrayList<Character> letters = new ArrayList();
	boolean centroOcupado = false;
	private PalabraRepository palabrasRepo;
	private List<Cadena> cadenasPendientes = new ArrayList<>();
	private List<JSONObject> jugadaPendiente;

	public Board() {
		this.palabrasRepo = Manager.get().getPalabrasRepo();
		this.initializeLetters();
		this.createBoard();
	}
	
	public Board (boolean testing) {
		this.palabrasRepo = Manager.get().getPalabrasRepo();
		if(testing) 
			this.initializeTestingLetters();
		else
			this.initializeLetters();
		
		this.createBoard();
	}
	
	
	private void createBoard() {
		// Crear las 255 casillas del tablero
		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++)
				squares[i][j] = new Square(i, j);
		int x, y;

		// Definir casillas de "triple valor de palabra"
		int[] tp = new int[] { 0, 2, 0, 12, 2, 0, 2, 14, 12, 0, 12, 14, 14, 2, 14, 12 };
		for (int i = 0; i < tp.length; i = i + 2) {
			x = tp[i];
			y = tp[i + 1];
			squares[x][y].setType(SquareType.TP);
		}

		// Definir casillas de "triple valor de letra"
		tp = new int[] { 0, 4, 0, 10, 1, 1, 1, 13, 2, 6, 2, 8, 3, 3, 3, 11, 4, 0, 4, 14, 5, 5, 5, 9, 6, 2, 6, 12, 8, 2,
				8, 12, 9, 5, 9, 9, 10, 0, 10, 14, 11, 3, 11, 11, 12, 6, 12, 8, 13, 1, 13, 13, 14, 4, 14, 10 };
		for (int i = 0; i < tp.length; i = i + 2) {
			x = tp[i];
			y = tp[i + 1];
			squares[x][y].setType(SquareType.TL);
		}

		// Definir casillas de "doble valor de palabra"
		tp = new int[] { 1, 5, 1, 9, 3, 7, 5, 1, 5, 13, 7, 3, 7, 11, 9, 1, 9, 13, 11, 7, 13, 5, 13, 9 };
		for (int i = 0; i < tp.length; i = i + 2) {
			x = tp[i];
			y = tp[i + 1];
			squares[x][y].setType(SquareType.DP);
		}

		// Definir casillas de "doble valor de letra"
		tp = new int[] { 2, 2, 2, 12, 4, 6, 4, 8, 6, 4, 6, 10, 8, 4, 8, 10, 10, 6, 10, 8, 12, 2, 12, 12 };
		for (int i = 0; i < tp.length; i = i + 2) {
			x = tp[i];
			y = tp[i + 1];
			squares[x][y].setType(SquareType.DL);
		}
	}

	public MovementResult movement(List<JSONObject> jugada) throws JSONException {

		List<Cadena> cadenas = new ArrayList<>();
		MovementResult resultado = new MovementResult("RESULT");
		boolean enVertical;
		try {
			enVertical = comprobarUnSoloSentido(jugada);
			this.colocarLetras(jugada);
			if (!centroOcupado)
				cadenas = primeraJugada(jugada);
			else {
				comprobarAdyacencia(jugada);
				cadenas.addAll(construirCadenas(jugada, enVertical));
			}
		} catch (Exception e) {
			resultado.addException(e.getMessage());
			return resultado;
		}

		for (Cadena cadena : cadenas) {
			if (cadena.length() == 1) {
				resultado.addAccepted(cadena);
			} else {
				List<Palabra> palabras = this.palabrasRepo.findByTexto(cadena.getText());
				if (palabras.isEmpty())
					resultado.addNotAccepted(cadena);
				else
					resultado.addAccepted(cadena);
			}
		}

		// Guardamos las cadenas formadas y quitamos las letras
		// del tablero hasta que se confirme la jugada
		this.cadenasPendientes = cadenas;
		this.jugadaPendiente = jugada;
		if (resultado.acceptsAll()) {
			calcularPuntos(cadenas);
		}

		return resultado;
	}

	private List<Cadena> construirCadenas(List<JSONObject> jugada, boolean enVertical) throws JSONException {
		Cadena cadena;
		JSONObject casilla;
		int row = jugada.get(0).getInt("row");
		int col = jugada.get(0).getInt("col");
		ArrayList<Cadena> cadenas = new ArrayList<>();
		if (enVertical)
			cadena = construirCadenaVertical(row, col);
		else
			cadena = construirCadenaHorizontal(row, col);
		cadenas.add(cadena);

		for (int i = 0; i < jugada.size(); i++) {
			casilla = jugada.get(i);
			row = casilla.getInt("row");
			col = casilla.getInt("col");
			cadena = enVertical ? construirCadenaHorizontal(row, col) : construirCadenaVertical(row, col);
			cadenas.add(cadena);
		}
		return cadenas;
	}

	private Cadena construirCadenaVertical(int row, int col) {
		int start = row;
		while (start > 0 && !this.squares[start][col].isEmpty()) {
			start--;
		}

		int end = row;
		while (end < 15 && !this.squares[end][col].isEmpty()) {
			end++;
		}
		Cadena cadena = new Cadena();
		Square casilla;
		for (int i = start; i < end; i++) {
			casilla = this.squares[i][col];
			if (!casilla.isEmpty())
				cadena.add(casilla);
		}
		return cadena;
	}

	private Cadena construirCadenaHorizontal(int row, int col) {
		Square casilla;
		int start = col;
		while (start > 0 && !this.squares[row][start].isEmpty()) {
			start--;
		}

		int end = col;
		while (end < 15 && !this.squares[row][end].isEmpty()) {
			end++;
		}
		Cadena cadena = new Cadena();
		for (int i = start; i < end; i++) {
			casilla = this.squares[row][i];
			if (!casilla.isEmpty())
				cadena.add(casilla);
		}
		return cadena;
	}

	private void comprobarAdyacencia(List<JSONObject> jugada) throws Exception {
		JSONObject casilla;
		int row, col;
		for (int i = 0; i < jugada.size(); i++) {
			casilla = jugada.get(i);
			row = casilla.getInt("row");
			col = casilla.getInt("col");
			if (existeAdyacente(row, col))
				return;
		}
		throw new Exception("Fichas mal posicionadas");
	}

	private void colocarLetras(List<JSONObject> jugada) throws Exception {
		int row, col;
		for (int i = 0; i < jugada.size(); i++) {
			JSONObject casilla = jugada.get(i);
			row = casilla.getInt("row");
			col = casilla.getInt("col");
			if (!this.squares[row][col].isEmpty())
				throw new Exception("No puedes poner letras en casillas ocupadas");
			this.squares[row][col].setLetter(casilla.getString("letter").charAt(0));
		}
	}

	private void calcularPuntos(List<Cadena> cadenas) {
		for (int i = 0; i < cadenas.size(); i++) {
			Cadena cadena = cadenas.get(i);
			cadena.calculatePoints();
		}
	}
	
	//Quita las letras del tablero hasta que el jugador confirme la jugada
	public void quitarJugadaProvisional() throws JSONException {
		for (int i = 0; i < this.jugadaPendiente.size(); i++) {
			squares[this.jugadaPendiente.get(i).getInt("row")][jugadaPendiente.get(i).getInt("col")].setLetter('\0');
		}
	}

	public void confirmarJugada() throws JSONException {
		for (int i = 0; i < jugadaPendiente.size(); i++) {
			squares[jugadaPendiente.get(i).getInt("row")][jugadaPendiente.get(i).getInt("col")]
					.setLetter(jugadaPendiente.get(i).getString("letter").charAt(0));
		}
		
		for (int i = 0; i < this.cadenasPendientes.size(); i++) {
			Cadena cadena = this.cadenasPendientes.get(i);
			cadena.setProvisional(false);
		}
	}

	private boolean existeAdyacente(int row, int col) {
		int norte = row - 1;
		int sur = row + 1;
		int este = col + 1;
		int oeste = col - 1;
		if (norte >= 0 && !this.squares[norte][col].isEmpty() && !this.squares[norte][col].isProvisional())
			return true;
		if (sur <= 14 && !this.squares[sur][col].isEmpty() && !this.squares[sur][col].isProvisional())
			return true;
		if (este <= 14 && !this.squares[row][este].isEmpty() && !this.squares[row][este].isProvisional())
			return true;
		if (oeste >= 0 && !this.squares[row][oeste].isEmpty() && !this.squares[row][oeste].isProvisional())
			return true;
		return false;
	}

	private List<Cadena> primeraJugada(List<JSONObject> jugada) throws Exception {
		if (jugada.size() <= 1)
			throw new Exception("No puedes comenzar la partida con una sola letra");
		JSONObject jsoCasilla;
		for (int i = 0; i < jugada.size(); i++) {
			jsoCasilla = jugada.get(i);
			if (jsoCasilla.getInt("row") == 7 && jsoCasilla.getInt("col") == 7) {
				centroOcupado = true;
				break;
			}
		}
		if (!centroOcupado)
			throw new Exception("Debes empezar en la casilla central");
		Cadena cadena = getCadena(jugada);
		ArrayList<Cadena> cadenas = new ArrayList<>();
		cadenas.add(cadena);
		return cadenas;
	}

	private Cadena getCadena(List<JSONObject> jugada) throws JSONException {
		Cadena cadena = new Cadena();
		JSONObject casilla;
		int row, col;
		for (int i = 0; i < jugada.size(); i++) {
			casilla = jugada.get(i);
			row = casilla.getInt("row");
			col = casilla.getInt("col");
			cadena.add(this.squares[row][col]);
		}
		return cadena;
	}

	private boolean comprobarUnSoloSentido(List<JSONObject> jugada) throws Exception {
		boolean enVertical = false, enHorizontal = false;
		JSONObject jsoCasilla = jugada.get(0);
		int fila0 = jsoCasilla.getInt("row");
		int col0 = jsoCasilla.getInt("col");
		for (int i = 1; i < jugada.size(); i++) {
			jsoCasilla = jugada.get(i);
			enHorizontal = enHorizontal || jsoCasilla.getInt("row") == fila0;
			enVertical = enVertical || jsoCasilla.getInt("col") == col0;
		}
		if (enHorizontal && enVertical)
			throw new Exception("Fichas mal posicionadas");
		if (enHorizontal)
			Collections.sort(jugada, new JugadaComparatorByColumn());
		else
			Collections.sort(jugada, new JugadaComparatorByRow());
		return enVertical;
	}

	/**
	 * Inicializa el listado de letras ordenándolas de manera aleatoria
	 */
	private void initializeLetters() {
		for (Entry<Character, Integer> entry : LETTER_QUANTITY.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				letters.add(entry.getKey());
			}
		}
		// Shuffle letras
		Collections.shuffle(this.letters);
	}
	
	private void initializeTestingLetters() {
		
		//Turno 1: Jugador A (ESCUDO)
		letters.add('S');
		letters.add('C');
		letters.add('O');
		letters.add('D');
		letters.add('E');
		letters.add('E');
		letters.add('U');
		
		//Turno 2: Jugador B (CERA)
		letters.add('R');
		letters.add('E');
		letters.add('A');
		letters.add('S');
		letters.add('Ñ');
		letters.add('O');
		letters.add('P');
		
		//Turno 3: Jugador A recibe depués de ESCUDO (RETAN)
		letters.add('N');
		letters.add('T');
		letters.add('A');
		letters.add('R');
		letters.add('I');
		letters.add('A');
		
		
		//Turno 4: Jugador B recibe después de CERA (SALID)
		letters.add('A');
		letters.add('I');
		letters.add('L');
		
		
		//Turno 5: Jugador A recibe después de RETAN (AIRE)
		letters.add('L');
		letters.add('N');
		letters.add('I');
		letters.add('O');

		
		//Turno 6: Jugador B recibe después de SALID (PIÑA)
		letters.add('I');
		letters.add('A');
		letters.add('E');
		letters.add('A');
		
		//Turno 7: Jugador A recibe después de AIRE (SONIA)
		letters.add('A');
		letters.add('A');
		letters.add('S');
		
		
		//Turno 9: Jugador B recibe
		letters.add('R');
		letters.add('V');
		letters.add('N');
		
	}


	public String getLetters(int n) {
		String r = "";
		int i = 0;
		
		while (i<n && !this.letters.isEmpty()) {
			r = r + this.letters.remove(0) + " ";
			i++;
		}

		// Remove last space character
		if (r.length() > 1) {
			r = r.substring(0, r.length() - 1);
		}

		return r;
	}
	
	public String changeLetters(Character[] letters) {
		if(letters.length > this.availableLetters()) {
			return "";
		}
		
		// Devolver las letras del usuario al listado de letras
		for (Character letter : letters) {
			this.addLetter(letter);
		}
		
		return this.getLetters(letters.length);
		
	}

	public int availableLetters() {
		return this.letters.size();
	}

	public void addLetter(Character letter) {
		this.letters.add(letter);
		// Shuffle letras
		Collections.shuffle(this.letters);
	}

	public void addLettersStart(ArrayList<Character> letters) {
		this.letters.addAll(0, letters);
	}
	
	

}
