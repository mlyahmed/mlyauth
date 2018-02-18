package com.mlyauth.advisers;

import com.mlyauth.exception.IDPException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ErrorController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IDPException.class)
    public ModelAndView exception(HttpServletRequest request, IDPException error) {
        request.setAttribute("errorMessage", error.getMessage());
        request.setAttribute("MLY_AUTH_ERROR_CODE", "APP_NOT_ASSIGNED");
        final ModelAndView view = new ModelAndView("error/" + getStatus(request).value());
        view.setStatus(getStatus(request));
        return view;
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null)
            return HttpStatus.INTERNAL_SERVER_ERROR;
        return HttpStatus.valueOf(statusCode);
    }
}
