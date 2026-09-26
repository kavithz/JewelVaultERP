package com.jewelvaulterp.integration.exception;

import com.jewelvaulterp.integration.dto.IntegrationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class IntegrationExceptionHandler {

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<IntegrationErrorResponse> handleCompanyNotFound(
            CompanyNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
            InvalidIntegrationRequestException.class,
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<IntegrationErrorResponse> handleBadRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        String message = ex.getMessage() == null ? "Invalid request parameters" : ex.getMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<IntegrationErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<IntegrationErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<IntegrationErrorResponse> handleDataIntegrity(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", "Request could not be processed due to a data integrity constraint.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<IntegrationErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.", request.getRequestURI());
    }

    private ResponseEntity<IntegrationErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path
    ) {
        return ResponseEntity.status(status).body(new IntegrationErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                path
        ));
    }
}
