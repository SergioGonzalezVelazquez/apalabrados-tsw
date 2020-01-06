package apalabrados.dao;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import apalabrados.model.Palabra;

public interface PalabraRepository extends CrudRepository<Palabra, Integer> {

	List<Palabra> findByTexto(String texto);
}

