package com.example.urlshortener.web;

import java.util.List;

import com.example.urlshortener.service.InvalidUrlException;
import com.example.urlshortener.service.LinkExpiredException;
import com.example.urlshortener.service.LinkNotFoundException;
import com.example.urlshortener.service.ShortCodeAlreadyExistsException;
import com.example.urlshortener.service.ShortCodeGenerationException;
import com.example.urlshortener.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();
		return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, details);
	}

	@ExceptionHandler(InvalidUrlException.class)
	public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidUrlException ex, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	@ExceptionHandler(ShortCodeAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleDuplicate(ShortCodeAlreadyExistsException ex, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, ex.getMessage(), request);
	}

	@ExceptionHandler(LinkNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(LinkNotFoundException ex, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	@ExceptionHandler(LinkExpiredException.class)
	public ResponseEntity<ErrorResponse> handleExpired(LinkExpiredException ex, HttpServletRequest request) {
		return error(HttpStatus.GONE, ex.getMessage(), request);
	}

	@ExceptionHandler({ ShortCodeGenerationException.class, DataAccessException.class })
	public ResponseEntity<ErrorResponse> handleStorage(RuntimeException ex, HttpServletRequest request) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
	}

	private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
		return error(status, message, request, List.of());
	}

	private static ResponseEntity<ErrorResponse> error(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			List<String> details) {
		ErrorResponse response = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI(), details);
		return ResponseEntity.status(status).body(response);
	}
}
