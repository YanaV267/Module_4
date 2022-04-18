package com.epam.esm.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.springframework.http.HttpStatus.*;

/**
 * The type Base exception handler.
 *
 * @author YanaV
 * @project GiftCertificate
 */
@RestControllerAdvice
public class BaseExceptionHandler {
    private static final String NOT_FOUND_MESSAGE = "Requested resource not found (";
    private static final String NO_DATA_FOUND_MESSAGE = "No data was provided (";
    private static final String CLOSING_BRACE = ")";
    private static final String BAD_REQUEST_MESSAGE = "Request has failed (provided data was incorrect)";

    /**
     * Resource not found response entity.
     *
     * @param exception the exception
     * @return the response entity
     */
    @ExceptionHandler({NoHandlerFoundException.class, HttpClientErrorException.NotFound.class})
    public ResponseEntity<ResponseErrorEntity> resourceNotFound(NoHandlerFoundException exception) {
        return new ResponseEntity<>(new ResponseErrorEntity(NOT_FOUND.value(), Exception.class,
                NOT_FOUND_MESSAGE + exception.getMessage() + CLOSING_BRACE), NOT_FOUND);
    }

    /**
     * Data not found response entity.
     *
     * @param exception the exception
     * @return the response entity
     */
    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<ResponseErrorEntity> dataNotFound(NoDataFoundException exception) {
        return new ResponseEntity<>(new ResponseErrorEntity(NO_CONTENT.value(), exception.getResourceClass(),
                NO_DATA_FOUND_MESSAGE + exception.getParameters() + CLOSING_BRACE), NO_CONTENT);
    }

    /**
     * Request failed response error entity.
     *
     * @param exception the exception
     * @return the response error entity
     */
    @ExceptionHandler({BadRequestException.class, HttpClientErrorException.BadRequest.class,
            MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseErrorEntity requestFailed(BadRequestException exception) {
        return new ResponseErrorEntity(BAD_REQUEST.value(), exception.getResourceClass(), BAD_REQUEST_MESSAGE);
    }
}
