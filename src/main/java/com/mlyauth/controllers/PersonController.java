package com.mlyauth.controllers;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/domain/person")
public class PersonController {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ApplicationDAO applicationDAO;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody PersonBean newPerson(@RequestBody PersonBean person) {
        Person pers = new Person();
        BeanUtils.copyProperties(person, pers);
        pers = personDAO.save(pers);
        person.setId(pers.getId());
        return person;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody PersonBean updatePerson(@RequestBody PersonBean person) {
        Person pers = personDAO.findOne(person.getId());
        BeanUtils.copyProperties(person, pers);
        pers.setApplications(new HashSet<>());
        person.getApplications().stream().forEach(appname -> pers.getApplications().add(applicationDAO.findByAppname(appname)));
        personDAO.save(pers);
        return person;
    }

}
