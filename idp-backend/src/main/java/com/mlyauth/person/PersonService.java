package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.AuthenticationInfoStatus;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.AuthenticationInfoByLoginDAO;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.dao.PersonByEmailDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.AuthenticationInfoByLogin;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.PersonByEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class PersonService implements IPersonService {

    private static final int A_CENTURY = 1000 * 60 * 60 * 24 * 365 * 100;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Autowired
    private AuthenticationInfoByLoginDAO authInfoByLoginDAO;

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PersonByEmailDAO personByEmailDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private IPersonValidator personValidator;

    @Override
    public PersonBean createPerson(final PersonBean bean) {
        personValidator.validateNewPerson(bean);
        Person person = buildPerson(bean);
        person = personDAO.saveAndFlush(person);
        personByEmailDAO.saveAndFlush(PersonByEmail.newInstance().setPersonId(person.getExternalId())
                .setEmail(person.getEmail()));
        final AuthenticationInfo authInfo = authenticationInfoDAO.saveAndFlush(person.getAuthenticationInfo());
        authInfoByLoginDAO.saveAndFlush(AuthenticationInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));
        return personMapper.toBean(person);
    }

    private Person buildPerson(final PersonBean bean) {
        final Person person = personMapper.toEntity(bean).setAuthenticationInfo(newAuthenticationInfo(bean));
        person.getAuthenticationInfo().setPerson(person);
        return person;
    }

    private AuthenticationInfo newAuthenticationInfo(final PersonBean bean) {
        return AuthenticationInfo.newInstance()
                    .setLogin(bean.getEmail())
                    .setPassword(passwordEncoder.encode(getPassword(bean)))
                    .setStatus(AuthenticationInfoStatus.ACTIVE)
                    .setEffectiveAt(new Date())
                    .setExpireAt(new Date(System.currentTimeMillis() + A_CENTURY));
    }

    private String getPassword(final PersonBean bean) {
        return bean.getPassword() == null ? UUID.randomUUID().toString() : String.valueOf(bean.getPassword());
    }

    @Override
    public PersonBean updatePerson(final PersonBean bean) {
        final Person person = personDAO.findByExternalId(bean.getExternalId());
        return person != null ? personMapper.toBean(personDAO.save(personMapper.toEntity(bean))) : createPerson(bean);
    }

    @Override
    public void assignApplication(final String appname, final String personExternalId) {
        final Person person = personDAO.findByExternalId(personExternalId);
        final Application application = applicationDAO.findByAppname(appname);
        person.getApplications().add(application);
        personDAO.saveAndFlush(person);
    }

}
