package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.IDPException;
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
    private INewPersonService newPersonService;

    @PostMapping
    @PreAuthorize("hasPermission(#person, T(com.primasolutions.idp.permission.IDPPermission).CREATE)")
    public ResponseEntity newPerson(@RequestBody final PersonBean person) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(newPersonService.createPerson(person));
        } catch (IDPException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrors());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody PersonBean updatePerson(@RequestBody final PersonBean person) {
        return newPersonService.updatePerson(person);
    }

    @PutMapping("/_assign/{appname}/to/{personExternalId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void assignApplication(@PathVariable final String appname, @PathVariable final String personExternalId) {
        newPersonService.assignApplication(appname, personExternalId);
    }

}
