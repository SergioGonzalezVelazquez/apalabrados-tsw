package apalabrados.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import apalabrados.dao.PalabraRepository;
import apalabrados.web.controllers.Manager;

public class Board {
	private LettersCollection letters = new LettersCollection();
	private Square[][] squares = new Square[15][15];
	boolean centroOcupado = false;
	private Random dado = new Random();
	private PalabraRepository palabrasRepo;
	static HashMap<Character, Integer> puntos = new HashMap<>(); 
	
	static {
		puntos.put('A', 1); puntos.put('B', 3); puntos.put('C', 3); puntos.put('D', 2);
		puntos.put('E', 1); puntos.put('F', 4); puntos.put('G', 2); puntos.put('H', 4);
		puntos.put('I', 1); puntos.put('J', 8);                     puntos.put('L', 1);
		puntos.put('M', 3); puntos.put('N', 1); puntos.put('Ñ', 8); puntos.put('O', 1);
		puntos.put('P', 3); puntos.put('Q', 5); puntos.put('R', 1); puntos.put('S', 1);
		puntos.put('T', 1); puntos.put('U', 1); puntos.put('V', 4); 
		puntos.put('X', 8); puntos.put('Y', 4); puntos.put('Z', 10);
	}
	
	public Board() {
		this.palabrasRepo=Manager.get().getPalabrasRepo();
		this.disorderLetters();
		for (int i=0; i<15; i++) 
			for (int j=0; j<15; j++)
				squares[i][j] = new Square();
		int x, y;
		int[] tp=new int[] {0, 2, 0, 12, 2, 0, 2, 14, 12, 0, 12, 14, 14, 2, 14, 12};
		for (int i=0; i<tp.length; i=i+2) {
			x=tp[i]; y=tp[i+1];
			squares[x][y].setType(Type.TP);
		}
		tp=new int[] {0, 4, 0, 10, 1, 1, 1, 13, 2, 6, 2, 8, 3, 3, 3, 11, 4, 0, 4, 14, 5, 5, 5, 9, 6, 2, 6, 12, 8, 2, 8, 12, 9, 5, 9, 9, 10, 0, 10, 14,
				11, 3, 11, 11, 12, 6, 12, 8, 13, 1, 13, 13, 14, 4, 14, 10};
		for (int i=0; i<tp.length; i=i+2) {
			x=tp[i]; y=tp[i+1];
			squares[x][y].setType(Type.TL);
		}
		tp=new int[] {1, 5, 1, 9, 3, 7, 5, 1, 5, 13, 7, 3, 7, 11, 9, 1, 9, 13, 11, 7, 13, 5, 13, 9};
		for (int i=0; i<tp.length; i=i+2) {
			x=tp[i]; y=tp[i+1];
			squares[x][y].setType(Type.DP);
		}
		tp=new int[] {2, 2, 2, 12, 4, 6, 4, 8, 6, 4, 6, 10, 8, 4, 8, 10, 10, 6, 10, 8, 12, 2, 12, 12};
		for (int i=0; i<tp.length; i=i+2) {
			x=tp[i]; y=tp[i+1];
			squares[x][y].setType(Type.DL);
		}
	}
	
	String getLetters(int n) {
		StringBuilder r = new StringBuilder();
		for (int i=0; i<n; i++)
			r.append(this.letters.remove(0).getLetter());
		return r.toString();
	}
	
	private void addLetter(int amount, char c) {
		this.letters.add(amount, c, puntos.get(c));
	}

	private void disorderLetters() {		
		addLetter(12, 'A'); addLetter(2, 'B'); addLetter(4, 'C'); addLetter(5, 'D');
		addLetter(12, 'E'); addLetter(1, 'F'); addLetter(2, 'G'); addLetter(2, 'H');
		addLetter(6, 'I');  addLetter(1, 'J');                    addLetter(4, 'L');
		addLetter(2, 'M');  addLetter(5, 'N'); addLetter(1, 'Ñ'); addLetter(9, 'O');
		addLetter(2, 'P');  addLetter(1, 'Q'); addLetter(5, 'R'); addLetter(6, 'S');
		addLetter(4, 'T');  addLetter(5, 'U'); addLetter(1, 'V'); 
		addLetter(1, 'X');  addLetter(1, 'Y'); addLetter(1, 'Z');		
		
		for (int i=0; i<300; i++) {
			int posA = dado.nextInt(letters.size());
			int posB = dado.nextInt(letters.size());
			Letter letraPosA = letters.get(posA);
			letters.set(posA, letters.get(posB));
			letters.set(posB, letraPosA);
		}
	}

	public ResultadoJugada movement(List<JSONObject> jugada) {
		List<Cadena> cadenas=new ArrayList<>();
		ResultadoJugada resultado = new ResultadoJugada();
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
			if (cadena.length()==1) {
				resultado.addAccepted(cadena);
			} else {
				List<Palabra> palabras=this.palabrasRepo.findByTexto(cadena.getText());
				if (palabras.isEmpty())
					resultado.addNotAccepted(cadena);
				else
					resultado.addAccepted(cadena);
			}
		}
		if (resultado.acceptsAll())
			confirmarJugada(cadenas);
		return resultado;
	}

	private List<Cadena> construirCadenas(List<JSONObject> jugada, boolean enVertical) throws JSONException {
		Cadena cadena;
		JSONObject casilla;
		int row=jugada.get(0).getInt("row");
		int col=jugada.get(0).getInt("col");
		ArrayList<Cadena> cadenas = new ArrayList<>();
		if (enVertical)
			cadena = construirCadenaVertical(row, col);
		else
			cadena = construirCadenaHorizontal(row, col);
		cadenas.add(cadena);
		
		for (int i=0; i<jugada.size(); i++) {
			casilla=jugada.get(i);
			row = casilla.getInt("row");
			col = casilla.getInt("col");
			cadena = enVertical ? construirCadenaHorizontal(row, col) : construirCadenaVertical(row, col);
			cadenas.add(cadena);
		}
		return cadenas;
	}

	private Cadena construirCadenaVertical(int row, int col) {
		int start = row;
		while (start>0 && !this.squares[start][col].isEmpty()) {
			start--;
		}
		
		int end = row;
		while (end<15 && !this.squares[end][col].isEmpty()) {
			end++;
		}
		Cadena cadena=new Cadena();
		Square casilla;
		for (int i=start; i<end; i++) {
			casilla = this.squares[i][col];
			if (!casilla.isEmpty())
				cadena.add(casilla);
		}
		return cadena;
	}

	private Cadena construirCadenaHorizontal(int row, int col) {
		Square casilla;
		int start = col;
		while (start>0 && !this.squares[row][start].isEmpty()) {
			start--;
		}
		
		int end = col;
		while (end<15 && !this.squares[row][end].isEmpty()) {
			end++;
		}
		Cadena cadena=new Cadena();
		for (int i=start; i<end; i++) {
			casilla = this.squares[row][i];
			if (!casilla.isEmpty())
				cadena.add(casilla);
		}
		return cadena;
	}

	private void comprobarAdyacencia(List<JSONObject> jugada) throws Exception {
		JSONObject casilla;
		int row, col;
		for (int i=0; i<jugada.size(); i++) {
			casilla = jugada.get(i);
			row = casilla.getInt("row");
			col = casilla.getInt("col");
			if (existeAdyacente(row, col))
				return;
		}
		throw new Exception("Fichas mal posicionadas");
	}

	private boolean existeAdyacente(int row, int col) {
		int norte = row-1;
		int sur = row+1;
		int este = col+1;
		int oeste = col-1;
		if (norte>=0 && !this.squares[norte][col].isEmpty() && !this.squares[norte][col].isProvisional())
			return true;
		if (sur<=14 && !this.squares[sur][col].isEmpty() && !this.squares[sur][col].isProvisional())
			return true;
		if (este<=14 && !this.squares[row][este].isEmpty() && !this.squares[row][este].isProvisional())
			return true;
		if (oeste>=0 && !this.squares[row][oeste].isEmpty() && !this.squares[row][oeste].isProvisional())
			return true;
		return false;
	}

	private void confirmarJugada(List<Cadena> cadenas) {
		for (int i=0; i<cadenas.size(); i++) {
			Cadena cadena = cadenas.get(i);
			cadena.calculatePoints();
		}
		for (int i=0; i<cadenas.size(); i++) {
			Cadena cadena = cadenas.get(i);
			cadena.setProvisional(false);
		}
	}

	private void colocarLetras(List<JSONObject> jugada) throws Exception {
		int row, col;
		for (int i=0; i<jugada.size(); i++) {
			JSONObject casilla = jugada.get(i);
			row=casilla.getInt("row");
			col=casilla.getInt("col");
			if (!this.squares[row][col].isEmpty())
				throw new Exception("No puedes poner letras en casillas ocupadas");
			this.squares[row][col].setLetter(casilla.getString("letter").charAt(0));
		}
	}

	private boolean comprobarUnSoloSentido(List<JSONObject> jugada) throws Exception {
		boolean enVertical = false, enHorizontal=false;
		JSONObject jsoCasilla = jugada.get(0);
		int fila0 = jsoCasilla.getInt("row");
		int col0 = jsoCasilla.getInt("col");
		for (int i=1; i<jugada.size(); i++) {
			jsoCasilla = jugada.get(i);
			enHorizontal = enHorizontal || jsoCasilla.getInt("row")==fila0;
			enVertical = enVertical || jsoCasilla.getInt("col")==col0;
		}
		if (enHorizontal && enVertical)
			throw new Exception("Fichas mal posicionadas");
		if (enHorizontal) 
			Collections.sort(jugada, new JugadaComparatorByColumn());
		else
			Collections.sort(jugada, new JugadaComparatorByRow());
		return enVertical;
	}

	private List<Cadena> primeraJugada(List<JSONObject> jugada) throws Exception {
		if (jugada.size()<=1)
			throw new Exception("No puedes comenzar la partida con una sola letra");
		JSONObject jsoCasilla;
		for (int i=0; i<jugada.size(); i++) {
			jsoCasilla=jugada.get(i);
			if (jsoCasilla.getInt("row")==7 && jsoCasilla.getInt("col")==7) {
				centroOcupado=true;
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
		for (int i=0; i<jugada.size(); i++) {
			casilla = jugada.get(i);
			row = casilla.getInt("row");
			col = casilla.getInt("col");
			cadena.add(this.squares[row][col]);
		}
		return cadena;
	}


}
