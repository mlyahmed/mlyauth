package com.mlyauth.api;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.AuthException;
import com.mlyauth.services.IPersonService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/domain/person")
public class PersonController {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private IPersonService personService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity newPerson(@RequestBody PersonBean bean) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(bean));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrors());
        }
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
