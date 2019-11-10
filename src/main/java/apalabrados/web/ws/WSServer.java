package apalabrados.web.ws;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import apalabrados.model.User;
import apalabrados.web.controllers.WebController;
import apalabrados.model.Match;


@Component
public class WSServer extends TextWebSocketHandler {
	private static ConcurrentHashMap<String, WebSocketSession> sessionsById=new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, WebSocketSession> sessionsByUser=new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessionsById.put(session.getId(), session);
		User user = (User) session.getAttributes().get("user");
		user.setWebSocketSession(session);
		
		
		/* Player player = (Player) session.getAttributes().get("player");
		String userName=player.getUserName();
		if (sessionsByUser.get(userName)!=null) 
			sessionsByUser.remove(userName);
		sessionsByUser.put(userName, session);
		System.out.println(userName);*/
	}
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		System.out.println(message.getPayload());
		JSONObject jso=new JSONObject(message.getPayload());
		String type = jso.getString("type");
		switch(type) {
		case "INICIAR PARTIDA":
			String idPartida = jso.getString("idPartida");
			Match match = WebController.inPlayMatches.get(idPartida);
			match.start();
			break;
		
		case "MOVIMIENTO": //el jugador ha puesto letras
			break;
		case "CAMBIO DE LETRAS": //el jugador ha puesto letras
			String letras = jso.getString("letras");
			//match.cambioDeLetras(session, letras);
			break;
		case "PASO DE TURNO": //el jugador ha puesto letras
			break;
		case "ABANDONO": //el jugador ha puesto letras
			break;
		
		}
	}

	private void sendError(WebSocketSession session, String message) throws Exception {
		JSONObject jso = new JSONObject();
		jso.put("TYPE", "ERROR");
		jso.put("MESSAGE", message);
		WebSocketMessage<?> wsMessage=new TextMessage(jso.toString());
		session.sendMessage(wsMessage);
	}
}

