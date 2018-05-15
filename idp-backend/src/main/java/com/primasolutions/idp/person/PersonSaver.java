package com.primasolutions.idp.person;

import com.primasolutions.idp.security.authentication.AuthenticationInfoSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonSaver {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PersonByEmailDAO personByEmailDAO;

    @Autowired
    private AuthenticationInfoSaver authenticationInfoSaver;

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
