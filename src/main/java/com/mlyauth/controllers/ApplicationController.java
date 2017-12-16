package com.mlyauth.controllers;

import com.mlyauth.beans.ApplicationBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/domain/application")
public class ApplicationController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody ApplicationBean newApplication(@RequestBody ApplicationBean application) {
        return application;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody ApplicationBean updateApplication(@RequestBody ApplicationBean application) {
        return application;
    }

}
