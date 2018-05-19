package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.AuthenticationInfoSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonSaver {

    @Autowired
    protected PersonDAO personDAO;

    @Autowired
    protected PersonByEmailDAO byEmailDAO;

    @Autowired
    protected AuthenticationInfoSaver authenticationInfoSaver;

    public void create(final Person p) {
        personDAO.saveAndFlush(p);
        byEmailDAO.saveAndFlush(PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail()));
        authenticationInfoSaver.create(p.getAuthenticationInfo());
    }


    public void update(final Person p) {
        personDAO.saveAndFlush(p);
    }
}
