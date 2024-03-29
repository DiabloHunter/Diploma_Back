package com.example.project.exceptions;

import com.example.project.common.ApiResponse;
import javassist.NotFoundException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {

    private static final Logger LOG = LogManager.getLogger(ExceptionControllerAdvice.class);

    @ExceptionHandler(value = AuthenticationFailException.class)
    public final ResponseEntity<ApiResponse> handleAuthenticationFailException(AuthenticationFailException exception) {
        LOG.error(exception.getMessage());
        return new ResponseEntity<>(new ApiResponse(false, exception.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(value = NotFoundException.class)
    public final ResponseEntity<ApiResponse> handleNotFoundException(NotFoundException exception) {
        LOG.error(exception.getMessage());
        return new ResponseEntity<>(new ApiResponse(false, exception.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public final ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        LOG.error(exception.getMessage());
        return new ResponseEntity<>(new ApiResponse(false, exception.getMessage()), HttpStatus.OK);
    }
}
