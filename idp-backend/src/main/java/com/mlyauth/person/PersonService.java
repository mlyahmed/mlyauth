package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.AuthenticationInfoStatus;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

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
        Person person = toEntity(bean);
        person.getAuthenticationInfo().setPerson(person);
        person = personDAO.saveAndFlush(person);
        authenticationInfoDAO.saveAndFlush(person.getAuthenticationInfo());
        return personMapper.toBean(person);
    }

    private Person toEntity(PersonBean bean) {
        return personMapper.toEntity(bean).setAuthenticationInfo(newAuthenticationInfo(bean));
    }

    private AuthenticationInfo newAuthenticationInfo(PersonBean bean) {
        return AuthenticationInfo.newInstance()
                    .setLogin(bean.getEmail())
                    .setPassword(passwordEncoder.encode(getPassword(bean)))
                    .setStatus(AuthenticationInfoStatus.ACTIVE)
                    .setEffectiveAt(new Date())
                    .setExpireAt(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 365 * 100)));
    }

    private String getPassword(PersonBean bean) {
        return bean.getPassword() == null ? UUID.randomUUID().toString() : String.valueOf(bean.getPassword());
    }

    @Override
    public PersonBean updatePerson(PersonBean bean) {
        final Person person = personDAO.findByExternalId(bean.getExternalId());
        return person != null ? personMapper.toBean(personDAO.save(personMapper.toEntity(bean))) : createPerson(bean);
    }

}
