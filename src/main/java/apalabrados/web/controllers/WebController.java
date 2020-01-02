package apalabrados.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import apalabrados.dao.TokenRepository;
import apalabrados.dao.UserRepository;
import apalabrados.model.User;
import apalabrados.utils.EMailSenderService;
import apalabrados.model.Token;
import apalabrados.web.exceptions.LoginException;
import apalabrados.web.exceptions.InvalidTokenException;
import apalabrados.model.Match;

@RestController
public class WebController {
	@Autowired
	private TokenRepository tokenRepo;
	@Autowired
	private UserRepository userRepo;
	
	private List<Match> pendingMatches = new ArrayList<>();
	private EMailSenderService emailSender = new EMailSenderService();
	public static ConcurrentHashMap<String, Match> inPlayMatches = new ConcurrentHashMap<>();

	@RequestMapping("/signup")
	public User signup(@RequestParam(value = "userName") String userName, @RequestParam(value = "email") String email,
			@RequestParam(value = "pwd1") String pwd1,
			@RequestParam(value = "pwd2") String pwd2) throws Exception {
		
		System.out.println("username: " + userName + ", email: " + email);
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
		System.out.println("Registro encript: " + pwdEncrypt);
		user.setPwd(pwdEncrypt);

		userRepo.save(user);
		System.out.println("register Ok: user " + user.getUserName() + " with email " + user.getEmail() );

		return user;
	}

	@RequestMapping("/login")
	public User login(HttpSession session, @RequestParam(value = "email") String email,
			@RequestParam(value = "pwd") String pwd) throws LoginException {
		User user;
		user = userRepo.findByEmail(email);
		String pwdEncrypt = DigestUtils.sha1Hex(pwd);
		System.out.println("Login encript: " + pwdEncrypt);
		if (user != null && user.getPwd().equals(pwdEncrypt)) {
			session.setAttribute("user", user);
			return user;
		}
		else
			throw new LoginException();
	}

	@RequestMapping("/logout")
	public void salir(HttpSession session) throws Exception {
		System.out.println("Logout");
		session.invalidate();
	}

	@PostMapping("/createMatch")
	public String createMatch(HttpSession session) throws Exception {
		System.out.println("create match");
		if (session.getAttribute("user") == null)
			throw new Exception("Identifícate antes de jugar");

		User user = (User) session.getAttribute("user");

		Match match = new Match();
		match.setPlayerA(user);
		this.pendingMatches.add(match);
		JSONObject jso = new JSONObject();
		jso.put("type", "PARTIDA CREADA");
		jso.put("idPartida", match.getId());
		return jso.toString();
	}

	@PostMapping("/joinMatch")
	public String joinMatch(HttpSession session) throws Exception {
		System.out.println("join match");
		if (session.getAttribute("user") == null)
			throw new Exception("Identifícate antes de jugar");

		User user = (User) session.getAttribute("user");

		if (this.pendingMatches.isEmpty())
			throw new Exception("No hay partidas pendientes. Crea una.");
		Match match = this.pendingMatches.remove(0);
		match.setPlayerB(user);
		this.inPlayMatches.put(match.getId(), match);
		JSONObject jso = new JSONObject();
		jso.put("type", "PARTIDA LISTA");
		jso.put("idPartida", match.getId());
		return jso.toString();
	}
	
	@PostMapping("requestToken")
	public void solicitarToken(@RequestParam String email) throws MessagingException {
		System.out.println("Request Token solicitud recibida");
		User user;
		user = userRepo.findByEmail(email);
		if(user!= null) {
			Token token = new Token(email);
			tokenRepo.save(token);
			

			this.emailSender.enviarPorGmail(email, token.getToken());
			System.out.println("correo enviado");
		}
		else {
			System.out.println("NO CORREO: " + email);
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
		System.out.println(code);
		
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