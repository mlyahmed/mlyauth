package com.hohou.federation.idp.person;

import com.hohou.federation.idp.exception.IDPException;
import com.hohou.federation.idp.person.model.PersonBean;
import com.hohou.federation.idp.person.service.PersonService;
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
    @PreAuthorize("hasPermission(#person, T(com.hohou.federation.idp.permission.IDPPermission).CREATE)")
    public ResponseEntity postPerson(@RequestBody final PersonBean person) {
        try {
            personService.createPerson(person);
            return ResponseEntity.status(HttpStatus.CREATED).body(personService.lookupPerson(person.getExternalId()));
        } catch (IDPException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrors());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    PersonBean putPerson(@RequestBody final PersonBean person) {
        if (personService.lookupPerson(person.getExternalId()) != null)
            personService.updatePerson(person);
        else
            personService.createPerson(person);
        return personService.lookupPerson(person.getExternalId());
    }

    @PutMapping("/_assign/{appname}/to/{personExternalId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void assignApplication(@PathVariable final String appname, @PathVariable final String personExternalId) {
        personService.assignApplication(appname, personExternalId);
    }

}
