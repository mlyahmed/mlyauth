package com.mlyauth.advisers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exception(final Throwable throwable, final Model model) {
        model.addAttribute("errorMessage", throwable.getMessage());
        model.addAttribute("MLY_AUTH_ERROR_CODE", "APP_NOT_ASSIGNED");
        return "error";
    }

}
