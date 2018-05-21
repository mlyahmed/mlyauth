package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.model.PersonBean;
import com.primasolutions.idp.person.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/domain/person")
public class PersonController {

    @Autowired
    private PersonService personService;

    @PostMapping
    @PreAuthorize("hasPermission(#person, T(com.primasolutions.idp.permission.IDPPermission).CREATE)")
    public ResponseEntity postPerson(@RequestBody final PersonBean person) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(person));
        } catch (IDPException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrors());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    PersonBean putPerson(@RequestBody final PersonBean person) {
        if (personService.lookupPerson(person.getExternalId()) != null)
            return personService.updatePerson(person);
        else
            return personService.createPerson(person);
    }

    @PutMapping("/_assign/{appname}/to/{personExternalId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void assignApplication(@PathVariable final String appname, @PathVariable final String personExternalId) {
        personService.assignApplication(appname, personExternalId);
    }

}
