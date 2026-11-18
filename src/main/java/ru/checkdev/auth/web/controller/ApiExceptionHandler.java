package ru.checkdev.auth.web.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail duplicateUser(DataIntegrityViolationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Адрес электронной почты уже зарегистрирован");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidRequest(MethodArgumentNotValidException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Проверьте данные запроса");
        problem.setProperty("fields", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList());
        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail invalidCredentials(AuthenticationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalidPassword(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
