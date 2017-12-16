package com.mlyauth.controllers;

import com.mlyauth.beans.PersonBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/domain/person")
public class PersonController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody PersonBean newPerson(@RequestBody PersonBean person) {
        return person;
    }

}
