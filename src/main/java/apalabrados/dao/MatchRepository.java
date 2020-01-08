package apalabrados.dao;

import org.springframework.data.repository.CrudRepository;

import apalabrados.model.Match;

public interface MatchRepository extends CrudRepository<Match, String> {
}