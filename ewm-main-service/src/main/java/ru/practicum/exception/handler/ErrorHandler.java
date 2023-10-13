package ru.practicum.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.DateTimeValidationException;
import ru.practicum.model.error.ApiError;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataConflictException(final DataConflictException e) {
        return new ApiError(e.getMessage(), "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleDataNotFoundException(final DataNotFoundException e) {
        return new ApiError(e.getMessage(), "The required object was not found.",
                HttpStatus.NOT_FOUND.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTimeValidationException(final DateTimeValidationException e) {
        return new ApiError(e.getMessage(), "Incorrectly made request.",
                HttpStatus.NOT_FOUND.toString(), LocalDateTime.now());
    }
}
