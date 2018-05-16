package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.AuthenticationInfoSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonSaver {

    @Autowired
    protected PersonDAO personDAO;

    @Autowired
    protected PersonByEmailDAO personByEmailDAO;

    @Autowired
    protected AuthenticationInfoSaver authenticationInfoSaver;

    public void create(final Person p) {
        final Person person = personDAO.saveAndFlush(p);
        personByEmailDAO.saveAndFlush(PersonByEmail.newInstance()
                .setPersonId(person.getExternalId())
                .setEmail(person.getEmail()));
        authenticationInfoSaver.create(person.getAuthenticationInfo());
    }


    public void update(final Person p) {
        personDAO.saveAndFlush(p);
    }
}
