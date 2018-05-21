package com.primasolutions.idp.person.service;

import com.primasolutions.idp.application.ApplicationLookuper;
import com.primasolutions.idp.authentication.AuthenticationInfoBuilder;
import com.primasolutions.idp.person.PersonLookuper;
import com.primasolutions.idp.person.PersonMapper;
import com.primasolutions.idp.person.PersonSaver;
import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonBean;
import com.primasolutions.idp.person.validator.IPersonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    @Autowired
    private PersonSaver personSaver;

    @Autowired
    private PersonLookuper personLookuper;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private IPersonValidator personValidator;

    @Autowired
    private ApplicationLookuper applicationLookuper;

    @Autowired
    private AuthenticationInfoBuilder authInfoBuilder;

    @Override
    public PersonBean lookupPerson(final String externalId) {
        return personMapper.toBean(personLookuper.byExternalId(externalId));
    }

    @Override
    public PersonBean createPerson(final PersonBean bean) {
        personValidator.validateNew(bean);
        personSaver.create(personMapper.toEntity(bean).setAuthenticationInfo(authInfoBuilder.toEntity(bean)));
        return personMapper.toBean(personLookuper.byExternalId(bean.getExternalId()));
    }

    @Override
    public PersonBean updatePerson(final PersonBean bean) {
        personValidator.validateUpdate(bean);
        final Person person = personMapper.toEntity(bean);
        personSaver.update(person);
        return personMapper.toBean(personLookuper.byExternalId(person.getExternalId()));
    }

    @Override
    public void assignApplication(final String appname, final String personExternalId) {
        final Person person = personLookuper.byExternalId(personExternalId);
        person.getApplications().add(applicationLookuper.byName(appname));
        personSaver.update(person);
    }

}
