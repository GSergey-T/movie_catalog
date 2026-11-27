package com.moviecatalog.movie_catalog.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.DatabindException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
//    @ExceptionHandler(EntityNotFoundException.class)
//    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
//        return ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(ex.getMessage());
//    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, List<String>> errors = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(400, errors));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, List<String>> errors = new TreeMap<>();
        DatabindException mappingException = findMappingException(ex);
        
        String field = mappingException == null || mappingException.getPath().isEmpty()
                ? "request"
                : mappingException.getPath().stream()
                    .map(reference -> reference.getPropertyName() != null
                            ? reference.getPropertyName()
                            : "[" + reference.getIndex() + "]")
                    .reduce((left, right) -> right.startsWith("[") ? left + right : left + "." + right)
                    .orElse("request");
        
        String message = mappingException == null
                ? "Некорректный JSON или значение поля"
                : "Некорректное значение или формат поля: " + mappingException.getOriginalMessage();
        
        errors.put(field, List.of(message));
        
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(400, errors));
    }

    private DatabindException findMappingException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DatabindException mappingException) {
                return mappingException;
            }
            current = current.getCause();
        }
        return null;
    }

//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
//        Map<String, List<String>> errors = new TreeMap<>();
//        ex.getConstraintViolations().forEach(violation ->
//                errors.computeIfAbsent(violation.getPropertyPath().toString(), key -> new ArrayList<>())
//                        .add(violation.getMessage()));
//        return ResponseEntity.badRequest().body(new ValidationErrorResponse(400, errors));
//    }

    public record ValidationErrorResponse(int status, Map<String, List<String>> errors) {}
}
