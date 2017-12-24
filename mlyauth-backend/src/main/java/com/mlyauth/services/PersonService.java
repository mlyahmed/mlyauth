package com.mlyauth.services;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.AuthException;
import com.mlyauth.mappers.PersonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PersonService implements IPersonService {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private PersonMapper personMapper;


    public PersonBean createPerson(PersonBean bean) {

        if (bean == null)
            throw AuthException.newInstance();

        if (personDAO.findByUsername(bean.getUsername()) != null)
            throw AuthException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));

        Person person = personMapper.toEntity(bean);
        person.setPassword(passwordEncoder.encode(String.valueOf(bean.getPassword())));
        person = personDAO.save(person);
        return personMapper.toBean(person);
    }

}
