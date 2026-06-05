package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    /*Убедитесь, что ваше приложение возвращает корректные HTTP-коды:


    400
    400 — если ошибка валидации: ValidationException;

     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(final WrongArgumentException e) {
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }
    //404 — для всех ситуаций, если искомый объект не найден;
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundError(final NotFoundException e) {
        return new ErrorResponse("Объект не найден", e.getMessage());
    }
    //500 — если возникло исключение.
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleExceptionError(final RuntimeException e) {
        return new ErrorResponse("Возникло исключение", e.getMessage());
    }

}
