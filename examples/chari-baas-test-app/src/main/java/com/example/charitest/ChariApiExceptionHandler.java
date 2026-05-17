package com.example.charitest;

import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariBaasException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ChariApiExceptionHandler {

    @ExceptionHandler(ChariBaasException.class)
    public ResponseEntity<Map<String, Object>> handleChariError(ChariBaasException exception) {
        int status = exception.getHttpStatusCode() == null ? 502 : exception.getHttpStatusCode();

        return ResponseEntity.status(HttpStatus.valueOf(status)).body(Map.of(
                "message", exception.getMessage(),
                "stage", valueOrNull(exception.getStage()),
                "httpStatusCode", status,
                "errorCode", valueOrNull(exception.getErrorCode()),
                "errorDescription", valueOrNull(exception.getErrorDescription()),
                "knownErrorCode", String.valueOf(exception.getKnownErrorCode())));
    }

    private Object valueOrNull(Object value) {
        return value == null ? "null" : value;
    }
}
