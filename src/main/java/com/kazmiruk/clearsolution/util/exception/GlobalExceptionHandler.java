package com.kazmiruk.clearsolution.util.exception;

import com.kazmiruk.clearsolution.model.dto.ErrorDto;
import com.kazmiruk.clearsolution.model.exception.BadRequestException;
import com.kazmiruk.clearsolution.model.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto<Map<String, List<String>>>> handleValidationErrors(
            MethodArgumentNotValidException e
    ) {
        Map<String, List<String>> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            parseFieldError(error, errors);
        });

        ErrorDto<Map<String, List<String>>> errorDto = new ErrorDto<>(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    private void parseFieldError(ObjectError error, Map<String, List<String>> errors) {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        List<String> fieldErrors;
        if (errors.containsKey(fieldName)) {
            fieldErrors = errors.get(fieldName);
        } else {
            fieldErrors = new ArrayList<>();
        }
        fieldErrors.add(errorMessage);
        errors.put(fieldName, fieldErrors);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto<String>> handleBadRequestException(BadRequestException e) {
        ErrorDto<String> errorResponse = new ErrorDto<>(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto<String>> handleNotFoundException(NotFoundException e) {
        ErrorDto<String> errorResponse = new ErrorDto<>(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}
