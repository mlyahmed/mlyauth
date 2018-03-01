package com.mlyauth.api;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.exception.IDPException;
import com.mlyauth.services.domain.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

}
