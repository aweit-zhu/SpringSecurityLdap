package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.model.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleCustomException(Exception ex) {
        return ErrorResponse.builder()
    		    .message(ex.getMessage())
    		    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
    		    .timestamp(System.currentTimeMillis())
    		    .build();
    }

}
