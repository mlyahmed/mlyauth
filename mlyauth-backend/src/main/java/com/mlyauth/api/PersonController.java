package com.mlyauth.api;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.AuthError;
import com.mlyauth.mappers.PersonMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;

@RestController
@RequestMapping("/domain/person")
public class PersonController {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private PersonMapper personMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity newPerson(@RequestBody PersonBean bean) {

        if (personDAO.findByUsername(bean.getUsername()) != null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));

        Person person = personMapper.toEntity(bean);
        person.setPassword(passwordEncoder.encode(String.valueOf(bean.getPassword())));
        person = personDAO.save(person);

        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toBean(person));
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
