package ru.practicum.explorewithme.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Ошибка валидации: ", ex);
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(
                errors,
                "Ошибка валидации",
                "Неверные параметры запроса",
                HttpStatus.BAD_REQUEST
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        log.error("Неожиданная ошибка: ", ex);
        ApiError apiError = new ApiError(
                List.of(ex.getMessage()),
                "Неожиданная ошибка",
                "Произошла непредвиденная ошибка",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        log.error("Ресурс не найден: ", ex);
        ApiError apiError = new ApiError(
                List.of(ex.getMessage()),
                "Не найдено",
                "Запрашиваемый ресурс не найден",
                HttpStatus.NOT_FOUND
        );

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public final ResponseEntity<Object> handleForbiddenException(ForbiddenException ex) {
        log.error("Доступ запрещен: ", ex);
        ApiError apiError = new ApiError(
                List.of(ex.getMessage()),
                "Доступ запрещен",
                "У вас нет прав на доступ к этому ресурсу",
                HttpStatus.FORBIDDEN
        );

        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConflictException.class)
    public final ResponseEntity<Object> handleConflictException(ConflictException ex) {
        log.error("Конфликт: ", ex);
        ApiError apiError = new ApiError(
                List.of(ex.getMessage()),
                "Конфликт",
                "Произошел конфликт с текущим состоянием ресурса",
                HttpStatus.CONFLICT
        );

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Неверный аргумент: ", ex);
        ApiError apiError = new ApiError(
                List.of(ex.getMessage()),
                "Некорректный запрос",
                "Неверные параметры запроса",
                HttpStatus.BAD_REQUEST
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}
