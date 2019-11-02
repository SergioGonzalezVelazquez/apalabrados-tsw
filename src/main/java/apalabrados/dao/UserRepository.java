package apalabrados.dao;

import org.springframework.data.repository.CrudRepository;

import apalabrados.model.User;

public interface UserRepository extends CrudRepository<User, String> {
	User findByEmail(String email);

}
