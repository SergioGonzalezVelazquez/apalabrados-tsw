package apalabrados.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import apalabrados.dao.MatchRepository;
import apalabrados.dao.PalabraRepository;
import apalabrados.dao.TokenRepository;
import apalabrados.dao.UserRepository;
import apalabrados.model.User;
import apalabrados.utils.EMailSenderService;
import apalabrados.model.Token;
import apalabrados.web.exceptions.LoginException;
import apalabrados.web.exceptions.InvalidTokenException;
import apalabrados.model.Cadena;
import apalabrados.model.Match;

@RestController
public class WebController {
	@Autowired
	private TokenRepository tokenRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private PalabraRepository palabraRepo;
	@Autowired
	private MatchRepository matchRepo;
	
	private List<Match> pendingMatches = new ArrayList<>();
	private EMailSenderService emailSender = new EMailSenderService();
	public static ConcurrentHashMap<String, Match> inPlayMatches = new ConcurrentHashMap<>();
	
	//Flag utilizado para realizar test y que las letras devueltas no sean aleatorias
	private boolean TESTING = true;
	
	
	@Autowired
	public void loadPalabrasRepo() {
		Manager.get().setPalabrasRepo(palabraRepo);
	}
	
	@Autowired
	public void loadMatchRepo() {
		Manager.get().setMatchRepo(matchRepo);
	}
	

	@RequestMapping("/signup")
	public User signup(@RequestParam(value = "userName") String userName, @RequestParam(value = "email") String email,
			@RequestParam(value = "pwd1") String pwd1,
			@RequestParam(value = "pwd2") String pwd2) throws Exception {
		
		if (pwd1 == null || pwd2 == null)
			throw new Exception("Passwords could not be empty");
		if (!pwd1.equals(pwd2))
			throw new Exception("Passwords do not match");
		if (userRepo.findById(userName).isPresent() || userRepo.findByEmail(email) != null)
			throw new Exception("The user already exists");

		User user = new User();
		user.setEmail(email);
		user.setUserName(userName);
		
		//Encrypt password with SHA-1
		String pwdEncrypt = DigestUtils.sha1Hex(pwd1);
		user.setPwd(pwdEncrypt);

		userRepo.save(user);

		return user;
	}

	@RequestMapping("/login")
	public User login(HttpSession session, @RequestParam(value = "email") String email,
			@RequestParam(value = "pwd") String pwd) throws LoginException {
		User user;
		user = userRepo.findByEmail(email);
		String pwdEncrypt = DigestUtils.sha1Hex(pwd);
		if (user != null && user.getPwd().equals(pwdEncrypt)) {
			session.setAttribute("user", user);
			return user;
		}
		else
			throw new LoginException();
	}

	@RequestMapping("/loginWithGoogle")
	public User loginWithGoogle(HttpSession session, @RequestParam(value = "userName") String userName, 
			@RequestParam(value = "email") String email) throws LoginException {
		User user= new User();
		user.setEmail(email);
		user.setUserName(userName);
		// Si no está en la bbdd guarda el mail y el nick
		if (userRepo.findByEmail(email) == null) 
			userRepo.save(user);
		
		session.setAttribute("user", user);
		return user;
	}

	@RequestMapping("/logout")
	public void salir(HttpSession session) throws Exception {
		session.invalidate();
	}

	@PostMapping("/createMatch")
	public String createMatch(HttpSession session) throws Exception {
		if (session.getAttribute("user") == null)
			throw new Exception("Identifícate antes de jugar");
		
		User user = (User) session.getAttribute("user");
		Match match = new Match();
		
		if (this.TESTING) {
			match.setTesting(true);
		}
		
		match.setPlayerA(user);
		session.setAttribute("match", match);
		this.pendingMatches.add(match);
		JSONObject jso = new JSONObject();
		jso.put("type", "PARTIDA CREADA");
		jso.put("idPartida", match.getId());
		
		matchRepo.save(match);
		
		return jso.toString();
	}
	
	@GetMapping("/matches")
	public String getMatches(HttpSession session) throws Exception {
		JSONObject jsoResponse = new JSONObject();
		JSONObject jsoMatch;
		JSONArray jsa = new JSONArray();
		if (session.getAttribute("user") == null)
			throw new Exception("Identifícate antes de jugar");

		User user = (User) session.getAttribute("user");
		List<Match> matches = matchRepo.findByPlayerA(user);
		
		//Partidas creadas por el usuario (es el player A)
		for (Match match : matches) {
			jsoMatch = new JSONObject();
			jsoMatch.put("status", match.getStatus());
			jsoMatch.put("winner", match.isPlayerAWins());
			jsoMatch.put("opponent", match.getPlayerB().getUserName());
			jsoMatch.put("created", match.getCreated());
			jsa.put(jsoMatch);
		}

		matches = matchRepo.findByPlayerB(user);
		
		//Partidas a las que se ha unido (es el player B)
		for (Match match : matches) {
			jsoMatch = new JSONObject();
			jsoMatch.put("status", match.getStatus());
			jsoMatch.put("winner", match.isPlayerBWins());
			jsoMatch.put("opponent", match.getPlayerA().getUserName());
			jsoMatch.put("created", match.getCreated());
			jsa.put(jsoMatch);
		}
		
		jsoResponse.put("matches", jsa);
		return jsoResponse.toString();
	}


	@PostMapping("/joinMatch")
	public String joinMatch(HttpSession session) throws Exception {
		if (session.getAttribute("user") == null)
			throw new Exception("Identifícate antes de jugar");

		User user = (User) session.getAttribute("user");

		if (this.pendingMatches.isEmpty())
			throw new Exception("No hay partidas pendientes. Crea una.");
				
		Match match = this.pendingMatches.remove(0);
		match.setPlayerB(user);
		session.setAttribute("match", match);
		this.inPlayMatches.put(match.getId(), match);
		JSONObject jso = new JSONObject();
		jso.put("type", "PARTIDA LISTA");
		jso.put("idPartida", match.getId());
		
		matchRepo.save(match);
		
		return jso.toString();
	}
	
	@PostMapping("requestToken")
	public void solicitarToken(@RequestParam String email) throws MessagingException {
		User user;
		user = userRepo.findByEmail(email);
		if(user!= null) {
			Token token = new Token(email);
			tokenRepo.save(token);
			
			this.emailSender.enviarPorGmail(email, token.getToken());
		}
	}
	
	@PostMapping("updatePwd")
	public void actualizarPwd(@RequestParam String code, @RequestParam String pwd1, @RequestParam String pwd2) throws Exception {
		if(!pwd1.equals(pwd2)) {
			throw new Exception("...") ;
		}
		Optional<Token> optToken = tokenRepo.findById(code);
		if(optToken.isPresent()) {
			Token token = optToken.get();
			if(token.isCaducado()) {
				throw new InvalidTokenException();
			}
			User user = userRepo.findByEmail(token.getEmail());
			String pwdEncrypt = DigestUtils.sha1Hex(pwd1);
			user.setPwd(pwdEncrypt);
			userRepo.save(user);
		}		
	}
	
	/*
	 * Endpoint use to check if token is valid*
	 */
	@PostMapping("validateToken")
	public ResponseEntity<String> validateToken(@RequestParam String code) throws Exception {
		Optional<Token> optToken = tokenRepo.findById(code);
		if(optToken.isPresent()) {
			Token token = optToken.get();
			if(token.isCaducado()) {
				throw new InvalidTokenException();
			}
			return ResponseEntity.ok().build();	
		}
		else {
			throw new InvalidTokenException();
		}		
	}
	
	@PostMapping("/updatePhoto")
	public ResponseEntity<String> updatePhoto(HttpSession session, @RequestParam String base64Image) throws Exception {
		if (session.getAttribute("user") == null)
			throw new Exception("Identifícate para actualizar tu perfil");

		User user = (User) session.getAttribute("user");

		//Los datos recibidos deben ser de la forma data:image/[png|jpg|...];base64,.....
		//Primero comprobamos que la primera parte es de la forma data:image/[png|jpg|...]
		Pattern patron = Pattern.compile("^data:image\\/[a-z1-9]+;base64,");
		Matcher matcher = patron.matcher(base64Image);
		if(!matcher.find()) {
			throw new Exception("Entrada de archivo no válida");
		}
		
		//Después cogemos la parte base64
		String bytes = base64Image.split(",")[1];
		
		if(!Base64.isBase64(bytes)) {
			throw new Exception("Entrada de archivo no válida");
		}
		
		byte[] encoded = Base64.encodeBase64(base64Image.getBytes());
		user.setPhoto(encoded);
		userRepo.save(user);
		
		return ResponseEntity.ok().build();	
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(HttpServletRequest req, Exception ex) {
		ModelAndView result = new ModelAndView();
		result.setViewName("respuesta");
		result.addObject("exception", ex);
		result.setStatus(HttpStatus.BAD_REQUEST);
		return result;
	}

	@ExceptionHandler(LoginException.class)
	public ModelAndView handleLoginException(HttpServletRequest req, Exception ex) {
		ModelAndView result = new ModelAndView();
		result.setViewName("mensajeLogin");
		result.addObject("exception", ex);
		result.setStatus(HttpStatus.UNAUTHORIZED);
		return result;
	}
}