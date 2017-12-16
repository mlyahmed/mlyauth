package com.mlyauth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route/navigate")
public class NavigationController {

    @GetMapping("/to/{appname}")
    @ResponseStatus(HttpStatus.OK)
    public void newApplication(@PathVariable String appname) {
        SecurityContext context = SecurityContextHolder.getContext();
        final Authentication authentication = context.getAuthentication();
        authentication.getPrincipal();
    }

}
