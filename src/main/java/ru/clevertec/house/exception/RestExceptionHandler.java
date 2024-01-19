package ru.clevertec.house.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.clevertec.house.util.StatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiError> handleServiceException(ServiceException serviceException) {

        ApiError apiError = ApiError.builder()
                .errorMessage(serviceException.getMessage())
                .build();

        return new ResponseEntity<>(apiError, HttpStatusCode.valueOf(StatusCode.NOT_FOUND));
    }
}
