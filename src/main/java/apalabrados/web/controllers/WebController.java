package apalabrados.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import apalabrados.dao.UserRepository;
import apalabrados.model.User;
import apalabrados.web.exceptions.LoginException;
import apalabrados.model.Match;

@RestController
public class WebController {
	@Autowired
	private UserRepository userRepo;
	private List<Match> pendingMatches = new ArrayList<>();
	public static ConcurrentHashMap<String, Match> inPlayMatches = new ConcurrentHashMap<>();

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
		user.setPwd(pwd1);

		userRepo.save(user);
		System.out.println("register Ok");

		return user;
	}

	@RequestMapping("/login")
	public User login(HttpSession session, @RequestParam(value = "email") String email,
			@RequestParam(value = "pwd") String pwd) throws LoginException {
		User user;
		user = userRepo.findByEmail(email);
		if (user != null && user.getPwd().equals(pwd)) {
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