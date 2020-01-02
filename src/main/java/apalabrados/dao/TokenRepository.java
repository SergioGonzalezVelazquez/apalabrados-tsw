package apalabrados.dao;

import org.springframework.data.repository.CrudRepository;

import apalabrados.model.Token;

public interface TokenRepository extends CrudRepository<Token, String> {
}
