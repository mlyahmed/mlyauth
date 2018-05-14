package com.primasolutions.idp.person;

import com.primasolutions.idp.beans.PersonBean;
import com.primasolutions.idp.constants.AuthenticationInfoStatus;
import com.primasolutions.idp.dao.ApplicationDAO;
import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.Person;
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
    private PersonSaver personSaver;

    @Autowired
    private PersonLookuper personLookuper;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private IPersonValidator personValidator;

    @Override
    public PersonBean createPerson(final PersonBean bean) {
        personValidator.validateNew(bean);
        personSaver.create(personMapper.toEntity(bean).setAuthenticationInfo(newAuthenticationInfo(bean)));
        return personMapper.toBean(personLookuper.byExternalId(bean.getExternalId()));
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
        final Person person = personLookuper.byExternalId(bean.getExternalId());
        if (person != null) {
            personSaver.update(person);
            return personMapper.toBean(personLookuper.byExternalId(person.getExternalId()));
        } else {
            return createPerson(bean);
        }
    }

    @Override
    public void assignApplication(final String appname, final String personExternalId) {
        final Person person = personLookuper.byExternalId(personExternalId);
        final Application application = applicationDAO.findByAppname(appname);
        person.getApplications().add(application);
        personSaver.update(person);
    }

}
