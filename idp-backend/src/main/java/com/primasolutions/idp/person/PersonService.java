package com.primasolutions.idp.person;

import com.primasolutions.idp.beans.PersonBean;
import com.primasolutions.idp.constants.AuthenticationInfoStatus;
import com.primasolutions.idp.dao.ApplicationDAO;
import com.primasolutions.idp.dao.AuthenticationInfoByLoginDAO;
import com.primasolutions.idp.dao.AuthenticationInfoDAO;
import com.primasolutions.idp.dao.PersonByEmailDAO;
import com.primasolutions.idp.dao.PersonDAO;
import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.AuthenticationInfoByLogin;
import com.primasolutions.idp.domain.Person;
import com.primasolutions.idp.domain.PersonByEmail;
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
