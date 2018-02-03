package com.mlyauth.services;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.PersonDAO;
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
        personValidator.validateNewPerson(bean);
        return personMapper.toBean(
                personDAO.save(personMapper.toEntity(bean)
                        .setPassword(passwordEncoder.encode(String.valueOf(bean.getPassword()))))
        );
    }

    @Override
    public PersonBean updatePerson(PersonBean bean) {
        return personMapper.toBean(
                personDAO.save(personMapper.toEntity(bean)
                        .setPassword(personDAO.findOne(bean.getId()).getPassword()))
        );
    }

}
