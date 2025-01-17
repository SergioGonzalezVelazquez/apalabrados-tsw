package apalabrados.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN)
public class InvalidTokenException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidTokenException() {
		super("Token inválido o expirado");
	}
}
