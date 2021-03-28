package com.ubitricity.carparkubi.controllers;

import com.ubitricity.carparkubi.services.ChargingPointNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {
    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(IllegalStateException.class)
    public void handleConflict() {
        // Nothing to do
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)  // 409
    @ExceptionHandler(ChargingPointNotFoundException.class)
    public void handleChargingPointNotfound() {
        // Nothing to do
    }
}
