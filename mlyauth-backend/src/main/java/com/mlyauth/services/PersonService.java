package com.mlyauth.services;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.mappers.PersonMapper;
import com.mlyauth.validators.IPersonValidator;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class PersonService implements IPersonService {

    @Inject
    private PersonDAO personDAO;


    @Inject
    private PasswordEncoder passwordEncoder;


    @Inject
    private PersonMapper personMapper;

    @Inject
    private IPersonValidator personValidator;

    @Override
    public PersonBean createPerson(PersonBean bean) {
        personValidator.validate(bean);
        Person person = personMapper.toEntity(bean);
        person.setPassword(passwordEncoder.encode(String.valueOf(bean.getPassword())));
        person = personDAO.save(person);
        return personMapper.toBean(person);
    }

    @Override
    public PersonBean updatePerson(PersonBean bean) {
        Person pers = personMapper.toEntity(bean);
        pers.setPassword(personDAO.findOne(bean.getId()).getPassword());
        pers = personDAO.save(pers);
        return personMapper.toBean(pers);
    }

}
