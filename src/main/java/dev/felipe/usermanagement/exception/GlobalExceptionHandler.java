package dev.felipe.usermanagement.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists() {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Esse email já foi registrado."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataBaseError() {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Houve um erro no sistema, tente novamente mais tarde."));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFoundException() {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Usuário não encontrado. Tente outro ou registre-se."));
    }

    @ExceptionHandler(InvalidCredentials.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Usuário ou senha inválidos."));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleExpiredToken() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("code", "TOKEN_EXPIRED"));
    }

    @ExceptionHandler({JwtException.class, IllegalArgumentException.class })
    public ResponseEntity<Map<String, String>> handleJwtException() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Acesso negado: token de acesso inválido."));
    }

    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handlePhoneAlreadyExistsException() {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Esse telefone já foi registrado."));
    }


}
