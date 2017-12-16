package com.mlyauth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.mlyauth.beans.AttributeBean.BASIC_AUTH_ENDPOINT;
import static com.mlyauth.beans.AttributeBean.BASIC_AUTH_PASSWORD;
import static com.mlyauth.beans.AttributeBean.BASIC_AUTH_USERNAME;

@RestController
@RequestMapping("/route/navigate")
public class NavigationController {

    @GetMapping("/to/{appname}")
    @ResponseStatus(HttpStatus.OK)
    public void newApplication(@PathVariable String appname, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute(BASIC_AUTH_ENDPOINT.getCode(), BASIC_AUTH_ENDPOINT.setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
        request.setAttribute(BASIC_AUTH_USERNAME.getCode(), BASIC_AUTH_USERNAME.setAlias("j_username").setValue("gestF"));
        request.setAttribute(BASIC_AUTH_PASSWORD.getCode(), BASIC_AUTH_PASSWORD.setAlias("j_password").setValue("gestF"));
        request.getRequestDispatcher("/route/navigate/post").forward(request, response);
    }

}
