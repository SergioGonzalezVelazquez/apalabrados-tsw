package apalabrados.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import apalabrados.model.Match;
import apalabrados.model.User;

public interface MatchRepository extends CrudRepository<Match, String> {
	List<Match> findByPlayerA(User playerA);
	List<Match> findByPlayerB(User playerB);
}