package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.model.ApiError;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTimeValidationException(final DateTimeValidationException e) {
        return new ApiError(e.getMessage(), "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now());
    }
}
