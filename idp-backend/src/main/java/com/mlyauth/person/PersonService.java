package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.AuthenticationInfoStatus;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class PersonService implements IPersonService {

    @Autowired
    private ApplicationDAO applicationDAO;

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
        Person person = buildPerson(bean);
        person = personDAO.saveAndFlush(person);
        authenticationInfoDAO.saveAndFlush(person.getAuthenticationInfo());
        return personMapper.toBean(person);
    }

    private Person buildPerson(PersonBean bean) {
        final Person person = personMapper.toEntity(bean).setAuthenticationInfo(newAuthenticationInfo(bean));
        person.getAuthenticationInfo().setPerson(person);
        return person;
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

    @Override
    public void assignApplication(String appname, String personExternalId) {
        final Person person = personDAO.findByExternalId(personExternalId);
        final Application application = applicationDAO.findByAppname(appname);
        person.getApplications().add(application);
        personDAO.saveAndFlush(person);
    }

}
