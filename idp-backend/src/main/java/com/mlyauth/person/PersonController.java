package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.exception.IDPException;
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
    private IPersonService personService;

    @PostMapping
    @PreAuthorize("hasPermission(#person, T(com.mlyauth.security.functions.IDPPermission).CREATE)")
    public ResponseEntity newPerson(@RequestBody PersonBean person) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(person));
        } catch (IDPException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrors());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody PersonBean updatePerson(@RequestBody PersonBean person) {
        return personService.updatePerson(person);
    }

    @PutMapping("/_assign/{appname}/to/{personExternalId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void assignApplication(@PathVariable String appname, @PathVariable String personExternalId) {
        personService.assignApplication(appname, personExternalId);
    }

}
