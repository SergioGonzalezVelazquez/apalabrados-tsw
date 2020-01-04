package apalabrados.web.controllers;

import apalabrados.dao.PalabraRepository;
import apalabrados.model.*;

public class Manager {
	
	private PalabraRepository palabrasRepo;

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
}
