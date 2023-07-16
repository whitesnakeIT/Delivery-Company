package com.kapusniak.tomasz.handler;

import com.kapusniak.tomasz.openapi.model.ApiError;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ApiErrorService apiErrorService;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        ApiError apiError = apiErrorService.createApiError(ex);

        return new ResponseEntity<>(apiError, HttpStatus.OK);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        ApiError apiError = apiErrorService.createApiError(ex);

        return new ResponseEntity<>(apiError, HttpStatus.OK);
    }

    @ExceptionHandler(StaleObjectStateException.class)
    protected ResponseEntity<Object> handleStaleObjectState(StaleObjectStateException ex) {
        ApiError apiError = apiErrorService.createApiError(ex);

        return new ResponseEntity<>(apiError, HttpStatus.OK);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = apiErrorService.createApiError(ex);

        return new ResponseEntity<>(apiError, HttpStatus.OK);
    }
}
