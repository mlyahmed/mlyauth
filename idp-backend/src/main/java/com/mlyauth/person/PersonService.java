package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.AuthenticationInfoStatus;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PersonService implements IPersonService {

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

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
        final Person person = personDAO.save(toEntity(bean));
        authenticationInfoDAO.save(person.getAuthenticationInfo().setPerson(person));
        return personMapper.toBean(person);
    }

    private Person toEntity(PersonBean bean) {
        final AuthenticationInfo authenticationInfo = AuthenticationInfo.newInstance()
                .setLogin(bean.getEmail())
                .setPassword(passwordEncoder.encode(String.valueOf(bean.getPassword())))
                .setStatus(AuthenticationInfoStatus.ACTIVE)
                .setEffectiveAt(new Date())
                .setExpireAt(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 365 * 100))); //A century
        final Person person = personMapper.toEntity(bean);
        person.setAuthenticationInfo(authenticationInfo);
        return person;
    }

    @Override
    public PersonBean updatePerson(PersonBean bean) {
        final Person person = personDAO.findOne(bean.getId());
        final Person update = personMapper.toEntity(bean);
        update.setAuthenticationInfo(person.getAuthenticationInfo());
        return personMapper.toBean(personDAO.save(update));
    }

}
