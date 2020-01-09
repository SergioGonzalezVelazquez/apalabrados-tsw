package apalabrados.web.controllers;

import java.util.concurrent.ConcurrentHashMap;

import apalabrados.dao.MatchRepository;
import apalabrados.dao.PalabraRepository;
import apalabrados.model.*;

public class Manager {
	
	private PalabraRepository palabrasRepo;
	private MatchRepository matchRepo;

	private Manager() {
	}
	
	private static class ManagerHolder {
		static Manager singleton=new Manager();
	}
	
	public static Manager get() {
		return ManagerHolder.singleton;
	}

	public void setPalabrasRepo(PalabraRepository palabraRepo) {
		this.palabrasRepo=palabraRepo;
	}

	public PalabraRepository getPalabrasRepo() {
		return palabrasRepo;
	}
	
	public void setMatchRepo(MatchRepository matchRepo) {
		this.matchRepo=matchRepo;
	}

	
	public MatchRepository getMatchRepo() {
		return matchRepo;
	}
	
	
	public void setInPlayMatches(ConcurrentHashMap<String, Match> inPlayMatches) {
		// TODO Auto-generated method stub		
	}
	
	public void endMatch(String idPartida) {
		WebController.inPlayMatches.remove(idPartida);
	}
}
