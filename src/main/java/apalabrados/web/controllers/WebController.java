package apalabrados.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import apalabrados.dao.UserRepository;
import apalabrados.model.User;
import web.exceptions.LoginException;

@RestController
public class WebController {
	@Autowired
	private UserRepository userRepo;
	
	@RequestMapping("/signup")
	public void signup (@RequestParam(value="userName") String userName,
			@RequestParam(value="email") String email, @RequestParam(value="pwd1") String pwd1, 
			@RequestParam(value="pwd2") String pwd2) throws Exception {
		
		if (pwd1==null || pwd2==null)
			throw new Exception("Passwords could not be empty");
		if (!pwd1.equals(pwd2))
			throw new Exception("Passwords do not match");
		if (userRepo.findById(userName).isPresent() || userRepo.findByEmail(email)!=null)
			throw new Exception("The user already exists");
		User user=new User();
		user.setEmail(email);
		user.setUserName(userName);
		user.setPwd(pwd1);
		userRepo.save(user);	
	}
	
	@RequestMapping("/login")
	public void login(HttpSession session, @RequestParam(value="email") String email,
			@RequestParam(value="pwd") String pwd) throws LoginException {
		User user;
		user=userRepo.findByEmail(email);
		if (user!=null && user.getPwd().equals(pwd)) {
			session.setAttribute("user", user);
		}
		
		else throw new LoginException();
	}
	
	
	@RequestMapping("/logout")
	public void salir(HttpSession session) throws Exception {
		session.invalidate();
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