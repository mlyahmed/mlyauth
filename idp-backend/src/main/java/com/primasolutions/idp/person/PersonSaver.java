package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.AuthenticationInfoSaver;
import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonByEmail;
import com.primasolutions.idp.person.model.PersonByEmailDAO;
import com.primasolutions.idp.person.model.PersonDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class PersonSaver {

    @Autowired
    protected PersonDAO personDAO;

    @Autowired
    protected PersonByEmailDAO byEmailDAO;

    @Autowired
    protected AuthenticationInfoSaver authenticationInfoSaver;

    public void create(final Person p) {
        Assert.notNull(p, "The person arg is mandatory.");
        Assert.notNull(p.getAuthenticationInfo(), "The person authentication arg is mandatory.");
        personDAO.saveAndFlush(p);
        byEmailDAO.saveAndFlush(PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail()));
        authenticationInfoSaver.create(p.getAuthenticationInfo());
    }


    public void update(final Person p) {
        personDAO.saveAndFlush(p);
    }
}
