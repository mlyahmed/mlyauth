package com.hohou.federation.idp.person.service;

import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.application.ApplicationLookuper;
import com.hohou.federation.idp.authentication.AuthenticationInfoBuilder;
import com.hohou.federation.idp.person.lookuper.PersonLookuper;
import com.hohou.federation.idp.person.mapper.PersonMapper;
import com.hohou.federation.idp.person.model.Person;
import com.hohou.federation.idp.person.model.PersonBean;
import com.hohou.federation.idp.person.saver.PersonSaver;
import com.hohou.federation.idp.person.validator.IPersonValidator;
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
    public void createPerson(final PersonBean bean) {
        personValidator.validateNew(bean);
        personSaver.create(personMapper.toEntity(bean).setAuthenticationInfo(authInfoBuilder.toEntity(bean)));
    }

    @Override
    public void updatePerson(final PersonBean bean) {
        personValidator.validateUpdate(bean);
        personSaver.update(personMapper.toEntity(bean));
    }

    @Override
    public void assignApplication(final String appname, final String personExternalId) {
        final Person person = personLookuper.byExternalIdOrError(personExternalId);
        final Application application = applicationLookuper.byNameOrError(appname);
        person.getApplications().add(application);
        personSaver.update(person);
    }

}
