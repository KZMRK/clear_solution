package com.kazmiruk.clearsolution.model.exception;

import com.kazmiruk.clearsolution.model.dto.UserDto;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.Set;

@Getter
public class FieldValidationException extends RuntimeException {

    private final Set<ConstraintViolation<UserDto>> errors;

    public FieldValidationException(Set<ConstraintViolation<UserDto>> errors) {
        this.errors = errors;
    }
}
