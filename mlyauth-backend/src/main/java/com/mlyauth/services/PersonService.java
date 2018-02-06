package com.mlyauth.services;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.mappers.PersonMapper;
import com.mlyauth.validators.IPersonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonService implements IPersonService {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private IPersonValidator personValidator;

    @Override
    public PersonBean createPerson(PersonBean bean) {
        personValidator.validateNewPerson(bean);
        return personMapper.toBean(personDAO.save(toEntity(bean)));
    }

    private Person toEntity(PersonBean bean) {
        return personMapper.toEntity(bean)
                .setPassword(passwordEncoder.encode(String.valueOf(bean.getPassword())));
    }

    @Override
    public PersonBean updatePerson(PersonBean bean) {
        return personMapper.toBean(
                personDAO.save(personMapper.toEntity(bean)
                        .setPassword(personDAO.findOne(bean.getId()).getPassword()))
        );
    }

}
