package com.mlyauth.controllers;

import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.AuthError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static com.mlyauth.beans.AttributeBean.*;

@RestController
@RequestMapping("/route/navigate")
public class NavigationController {


    @Autowired
    private PersonDAO personDAO;

    @GetMapping("/to/{appname}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity newApplication(@PathVariable String appname, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getPrincipal().toString();
        final Person person = personDAO.findByUsername(username);
        Collection<Application> applications = person.getApplications();
        if(applications.stream().noneMatch(app -> app.getAppname().equals(appname)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Arrays.asList(new AuthError("APP_NOT_ASSIGNED", "")));

        request.setAttribute(BASIC_AUTH_ENDPOINT.getCode(), BASIC_AUTH_ENDPOINT.setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
        request.setAttribute(BASIC_AUTH_USERNAME.getCode(), BASIC_AUTH_USERNAME.setAlias("j_username").setValue("gestF"));
        request.setAttribute(BASIC_AUTH_PASSWORD.getCode(), BASIC_AUTH_PASSWORD.setAlias("j_password").setValue("gestF"));
        request.getRequestDispatcher("/route/navigate/post").forward(request, response);
        return ResponseEntity.ok().build();
    }

}
