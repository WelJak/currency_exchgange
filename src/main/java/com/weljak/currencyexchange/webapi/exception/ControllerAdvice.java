package com.weljak.currencyexchange.webapi.exception;

import com.weljak.currencyexchange.domain.model.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(AccountNotFoundException.class)
    ResponseEntity<FailResponse> handleUserNotFound(Exception exception, ServletWebRequest webRequest) {
        log.error("Error occurred for request: {}", webRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(new FailResponse(HttpStatus.NOT_FOUND.value(), "User not found"), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ExternalServiceException.class, RateNotFoundException.class})
    ResponseEntity<FailResponse> handleExternalServiceError(Exception exception, ServletWebRequest webRequest) {
        log.error("Error occurred for request: {}", webRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(new FailResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "External service not responding"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    ResponseEntity<FailResponse> handleInternalServiceError(Exception exception, ServletWebRequest webRequest) {
        log.error("Error occurred for request: {}", webRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(new FailResponse(HttpStatus.BAD_REQUEST.value(), "Not enough funds on account"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RequestValidationException.class)
    ResponseEntity<FailResponse> handleRequestValidationError(Exception exception, ServletWebRequest webRequest) {
        log.error("Error occurred for request: {}", webRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(new FailResponse(HttpStatus.BAD_REQUEST.value(), "Bad request"), HttpStatus.BAD_REQUEST);
    }
}
