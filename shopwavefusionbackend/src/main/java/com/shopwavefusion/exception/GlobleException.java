package com.shopwavefusion.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobleException {

	@ExceptionHandler(UserException.class)
	public ResponseEntity<ErrorDetails> UserExceptionHandler(UserException ue, WebRequest req) {

		ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());

		return new ResponseEntity<ErrorDetails>(err, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(ProductException.class)
	public ResponseEntity<ErrorDetails> ProductExceptionHandler(ProductException ue, WebRequest req) {

		ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());

		return new ResponseEntity<ErrorDetails>(err, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(CartItemException.class)
	public ResponseEntity<ErrorDetails> CartItemExceptionHandler(CartItemException ue, WebRequest req) {

		ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());

		return new ResponseEntity<ErrorDetails>(err, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(OrderException.class)
	public ResponseEntity<ErrorDetails> OrderExceptionHandler(OrderException ue, WebRequest req) {

		ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());

		return new ResponseEntity<ErrorDetails>(err, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDetails> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException me) {
		ErrorDetails err = new ErrorDetails(me.getBindingResult().getFieldError().getDefaultMessage(),
				"validation error", LocalDateTime.now());
		return new ResponseEntity<ErrorDetails>(err, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDetails> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex,
			WebRequest req) {
		ErrorDetails err = new ErrorDetails("Malformed JSON request", req.getDescription(false), LocalDateTime.now());
		return new ResponseEntity<ErrorDetails>(err, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorDetails> httpRequestMethodNotSupportedExceptionHandler(
			HttpRequestMethodNotSupportedException ex, WebRequest req) {
		ErrorDetails err = new ErrorDetails(ex.getMessage(), req.getDescription(false), LocalDateTime.now());
		return new ResponseEntity<ErrorDetails>(err, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", "Endpoint not found");
		body.put("details", request.getDescription(false));
		body.put("timestamp", LocalDateTime.now().toString());
		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDetails> otherExceptionHandler(Exception e, WebRequest req) {
		ErrorDetails error = new ErrorDetails(e.getMessage(), req.getDescription(false), LocalDateTime.now());

		return new ResponseEntity<ErrorDetails>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
