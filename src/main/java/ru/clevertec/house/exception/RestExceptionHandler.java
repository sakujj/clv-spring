package ru.clevertec.house.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.clevertec.house.constant.StatusCode;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiError> handleServiceException(ServiceException serviceException) {

        ApiError apiError = ApiError.builder()
                .errorMessage(serviceException.getMessage())
                .build();

        return ResponseEntity
                .status(StatusCode.BAD_REQUEST)
                .body(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ApiError apiError = ApiError.builder()
                .errorMessage("The request contains invalid data")
                .validationErrors(ex.getFieldErrors().stream()
                        .map(fe ->  fe.getField() + " : " + fe.getDefaultMessage())
                        .toList())
                .build();

        return ResponseEntity
                .status(StatusCode.BAD_REQUEST)
                .body(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = ApiError.builder()
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity
                .status(StatusCode.BAD_REQUEST)
                .body(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = ApiError.builder()
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity
                .status(StatusCode.NOT_FOUND)
                .body(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = ApiError.builder()
                .errorMessage("""
                        %s. Incorrect property "%s" value : %s""".formatted(ex.getMessage(),  ex.getPropertyName(), ex.getValue())
                )
                .build();

        return ResponseEntity
                .status(StatusCode.BAD_REQUEST)
                .body(apiError);
    }
}
