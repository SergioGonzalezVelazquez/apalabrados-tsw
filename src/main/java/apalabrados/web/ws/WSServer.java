package apalabrados.web.ws;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import apalabrados.model.User;
import apalabrados.web.controllers.WebController;
import apalabrados.model.Match;

@Component
public class WSServer extends TextWebSocketHandler {
	private static ConcurrentHashMap<String, WebSocketSession> sessionsById = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, WebSocketSession> sessionsByUser = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessionsById.put(session.getId(), session);
		User user = (User) session.getAttributes().get("user");
		user.setWebSocketSession(session);
		
		Match match= (Match) session.getAttributes().get("match");
		System.out.println("match: ");
		System.out.println(match.getId());
		
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{
		
		Match match= (Match) session.getAttributes().get("match");
		match.logout(session.getId());		
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");
		String idPartida = jso.getString("idPartida");
		Match match = WebController.inPlayMatches.get(idPartida);

		switch (type) {
		case "INICIAR PARTIDA":
			match.start();
			break;

		case "MOVIMIENTO": // el jugador ha puesto letras
			match.playerPlays(session.getId(), jso.getJSONArray("jugada"));
			break;
			
		case "CONFIRMAR_JUGADA": //el jugador confirma la jugada después de la validación del servidor
			match.acceptMovement(session.getId());
			break;
			
		case "CAMBIO_LETRAS": // el jugador ha puesto letras
			JSONArray arrJson = jso.getJSONArray("letters");
			Character[] letters = new Character[arrJson.length()];
			for (int i = 0; i < letters.length; i++) {
				letters[i] = arrJson.getString(i).charAt(0);
			}
			match.changeLetters(session.getId(), letters);
			break;
		case "PASO_TURNO": // el jugador cambiar el turno
			match.toggleTurn(session.getId());
			break;
		case "ABANDONO": // el jugador ha puesto letras
			match.giveUp(session.getId());
			break;

		}
	}

	private void sendError(WebSocketSession session, String message) throws Exception {
		JSONObject jso = new JSONObject();
		jso.put("TYPE", "ERROR");
		jso.put("MESSAGE", message);
		WebSocketMessage<?> wsMessage = new TextMessage(jso.toString());
		session.sendMessage(wsMessage);
	}
}
